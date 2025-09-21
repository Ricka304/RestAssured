package utils;

import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.openxml4j.opc.PackageAccess;
import org.apache.poi.ooxml.util.SAXHelper;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.eventusermodel.*;
import org.apache.poi.xssf.model.SharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.xml.sax.*;
import org.xml.sax.helpers.DefaultHandler;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;

/**
 * StreamingBulkExcelProcessor (batched writer with periodic checkpoints)
 */
public class StreamingBulkExcelProcessor {

    public interface RowHandler {
        Map<String, String> process(Map<String, String> row, int rowIndex) throws Exception;
    }

    public static class Config {
        public final String inputPath;
        public final String inputSheetName;
        public final String outputPath;
        public final String outputSheetName;
        public final List<String> extraOutputHeaders;
        public final int threadCount;
        public final int queueCapacity;

        public Config(String inputPath, String inputSheetName,
                      String outputPath, String outputSheetName,
                      List<String> extraOutputHeaders,
                      int threadCount, int queueCapacity) {
            this.inputPath = Objects.requireNonNull(inputPath);
            this.inputSheetName = Objects.requireNonNull(inputSheetName);
            this.outputPath = Objects.requireNonNull(outputPath);
            this.outputSheetName = Objects.requireNonNull(outputSheetName);
            this.extraOutputHeaders = extraOutputHeaders != null ? extraOutputHeaders : Collections.emptyList();
            this.threadCount = Math.max(1, threadCount);
            this.queueCapacity = Math.max(10, queueCapacity);
        }
    }

    private static class OrderedFuture {
        final int rowIndex;
        final Future<Map<String, String>> future;
        final Map<String, String> inputRow;

        OrderedFuture(int rowIndex, Future<Map<String, String>> future, Map<String, String> inputRow) {
            this.rowIndex = rowIndex;
            this.future = future;
            this.inputRow = inputRow;
        }
    }

    /**
     * Main processing function.
     */
    public static void process(Config cfg, RowHandler handler) throws Exception {
        Objects.requireNonNull(cfg);
        Objects.requireNonNull(handler);

        ExecutorService exec = Executors.newFixedThreadPool(cfg.threadCount);
        BlockingQueue<OrderedFuture> writeQueue = new ArrayBlockingQueue<>(cfg.queueCapacity);

        // Writer thread: consumes writeQueue in order and writes to SXSSFWorkbook in batches
        Thread writerThread = new Thread(() -> {
            final int SXSSF_WINDOW = 200;       // SXSSF internal in-memory rows
            final int BATCH_SIZE = 10_000;     // batch size after which we write a batch and checkpoint

            try (SXSSFWorkbook outWb = new SXSSFWorkbook(SXSSF_WINDOW)) {
                outWb.setCompressTempFiles(true);
                Sheet outSheet = outWb.createSheet(cfg.outputSheetName);
                if (outSheet instanceof SXSSFSheet) ((SXSSFSheet) outSheet).trackAllColumnsForAutoSizing();

                final boolean[] headerWrittenRef = new boolean[]{false};
                @SuppressWarnings("unchecked")
                final List<String>[] inputHeadersRef = new List[]{null};

                int outRowIndex = 0;
                List<OrderedFuture> batchBuffer = new ArrayList<>(Math.min(BATCH_SIZE, 1024));
                int batchCounter = 0;

                while (true) {
                    OrderedFuture of = writeQueue.take(); // blocks
                    if (of.rowIndex == -1 && of.future == null) {
                        // poison pill -> flush remaining batch and finish
                        if (!batchBuffer.isEmpty()) {
                            outRowIndex = writeBatchToSheet(batchBuffer, outSheet, headerWrittenRef, inputHeadersRef, cfg.extraOutputHeaders, outRowIndex);
                            // checkpoint after final partial batch
                            checkpointWorkbook(outWb, cfg.outputPath);
                            batchCounter++;
                        }
                        break;
                    }

                    batchBuffer.add(of);

                    if (batchBuffer.size() >= BATCH_SIZE) {
                        outRowIndex = writeBatchToSheet(batchBuffer, outSheet, headerWrittenRef, inputHeadersRef, cfg.extraOutputHeaders, outRowIndex);
                        batchCounter++;

                        // checkpoint: write current workbook state to disk so user can open it mid-run
                        checkpointWorkbook(outWb, cfg.outputPath);
                    }
                }

                // autosize first few columns (limit to first 10)
                int colsToAuto = 0;
                Row firstRow = outSheet.getRow(0);
                if (firstRow != null) colsToAuto = Math.min(10, firstRow.getLastCellNum());
                for (int i = 0; i < colsToAuto; i++) {
                    outSheet.autoSizeColumn(i);
                }

                // final write to outputPath (overwrite checkpoint if exists)
                try (FileOutputStream fos = new FileOutputStream(cfg.outputPath)) {
                    outWb.write(fos);
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                throw new RuntimeException("Writer thread failed: " + e.getMessage(), e);
            }
        }, "StreamingExcel-Writer");

        writerThread.start();

        // Now read the input .xlsx using XSSF and SAX (streaming)
        try (OPCPackage pkg = OPCPackage.open(new File(cfg.inputPath), PackageAccess.READ)) {
            XSSFReader reader = new XSSFReader(pkg);
            StylesTable styles = reader.getStylesTable();
            SharedStringsTable sst = (SharedStringsTable) reader.getSharedStringsTable();

            XSSFReader.SheetIterator sheets = (XSSFReader.SheetIterator) reader.getSheetsData();
            boolean sheetFound = false;

            while (sheets.hasNext()) {
                try (InputStream sheetStream = sheets.next()) {
                    String sheetName = sheets.getSheetName();
                    if (!sheetName.equalsIgnoreCase(cfg.inputSheetName)) {
                        continue;
                    }
                    sheetFound = true;

                    XMLReader parser = SAXHelper.newXMLReader();
                    SheetHandler sheetHandler = new SheetHandler(sst, styles, handler, exec, writeQueue);
                    parser.setContentHandler(sheetHandler);
                    parser.parse(new InputSource(sheetStream));

                    break; // processed the requested sheet
                }
            }

            if (!sheetFound) throw new IllegalArgumentException("Sheet not found: " + cfg.inputSheetName);
        } finally {
            // signal writer to finish
            writeQueue.put(new OrderedFuture(-1, null, Collections.emptyMap()));
            // shutdown executor and wait
            exec.shutdown();
            exec.awaitTermination(10, TimeUnit.MINUTES);
            // wait for writer to finish
            writerThread.join();
        }
    }

    /**
     * Writes an entire batch to the SXSSF sheet in order and clears the batch.
     * Returns the updated outRowIndex after writing.
     */
    private static int writeBatchToSheet(List<OrderedFuture> batchBuffer,
                                         Sheet outSheet,
                                         boolean[] headerWrittenRef,
                                         List<String>[] inputHeadersRef,
                                         List<String> extraOutputHeaders,
                                         int outRowIndex) {
        for (OrderedFuture of : batchBuffer) {
            if (!headerWrittenRef[0]) {
                inputHeadersRef[0] = new ArrayList<>(of.inputRow.keySet());
                List<String> allHeaders = new ArrayList<>(inputHeadersRef[0]);
                allHeaders.addAll(extraOutputHeaders);
                Row hdr = outSheet.createRow(outRowIndex++);
                for (int c = 0; c < allHeaders.size(); c++) {
                    hdr.createCell(c, CellType.STRING).setCellValue(allHeaders.get(c));
                }
                headerWrittenRef[0] = true;
            }

            Map<String, String> extra;
            try {
                extra = of.future.get(); // wait for processing result
            } catch (ExecutionException ee) {
                extra = Map.of("error", ee.getCause() == null ? ee.getMessage() : ee.getCause().toString());
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                extra = Map.of("error", "interrupted");
            }

            Row or = outSheet.createRow(outRowIndex++);
            int col = 0;
            for (String h : inputHeadersRef[0]) {
                String v = of.inputRow.getOrDefault(h, "");
                or.createCell(col++, CellType.STRING).setCellValue(v);
            }
            for (String h : extraOutputHeaders) {
                String v = extra != null ? extra.getOrDefault(h, "") : "";
                or.createCell(col++, CellType.STRING).setCellValue(v);
            }
        }

        batchBuffer.clear();
        return outRowIndex;
    }

    /**
     * Create a checkpoint by writing the current SXSSFWorkbook state to a temp file then moving to outputPath.
     * This makes the outputPath file readable mid-run. Non-fatal exceptions are logged to stderr.
     */
    private static void checkpointWorkbook(SXSSFWorkbook outWb, String outputPath) {
        Path tmp = Paths.get(outputPath + ".checkpoint");
        Path target = Paths.get(outputPath);
        try {
            // write to temp first
            try (FileOutputStream fos = new FileOutputStream(tmp.toFile())) {
                outWb.write(fos);
            }
            // atomically replace target (best-effort)
            try {
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException amnse) {
                // fallback if atomic move not supported
                Files.move(tmp, target, StandardCopyOption.REPLACE_EXISTING);
            }
            System.out.printf("Checkpoint saved: %s%n", target.toString());
        } catch (Exception e) {
            System.err.printf("Checkpoint failed: %s%n", e.getMessage());
            try { Files.deleteIfExists(tmp); } catch (Exception ignore) {}
        }
    }

    /**
     * SAX handler => parses rows into Map<header->value> and submits tasks to executor.
     */
    private static class SheetHandler extends DefaultHandler {
        private final SharedStringsTable sst;
        private final StylesTable stylesTable;
        private final RowHandler handler;
        private final ExecutorService exec;
        private final BlockingQueue<OrderedFuture> writeQueue;

        // SAX parsing state
        private final StringBuilder lastContentsBuilder = new StringBuilder();
        private boolean nextIsString = false;
        private int currentRow = -1;
        private int currentCol = -1;
        private List<String> headers = new ArrayList<>();
        private Map<Integer, String> rowCellMap = new LinkedHashMap<>();

        private int submissionIndex = 0;

        SheetHandler(SharedStringsTable sst, StylesTable stylesTable, RowHandler handler,
                     ExecutorService exec, BlockingQueue<OrderedFuture> writeQueue) {
            this.sst = sst;
            this.stylesTable = stylesTable;
            this.handler = handler;
            this.exec = exec;
            this.writeQueue = writeQueue;
        }

        private int nameToColumn(String name) {
            int column = -1;
            for (int i = 0; i < name.length(); ++i) {
                int c = name.charAt(i);
                if (Character.isDigit(c)) break;
                column = (column + 1) * 26 + (c - 'A');
            }
            return column;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) {
            if ("row".equals(qName)) {
                String r = attributes.getValue("r");
                currentRow = r != null ? Integer.parseInt(r) - 1 : currentRow + 1;
                rowCellMap.clear();
                currentCol = -1;
            } else if ("c".equals(qName)) {
                String ref = attributes.getValue("r");
                currentCol = ref != null ? nameToColumn(ref) : currentCol + 1;
                nextIsString = "s".equals(attributes.getValue("t"));
                lastContentsBuilder.setLength(0);
            }
        }

        @Override
        public void characters(char[] ch, int start, int length) {
            lastContentsBuilder.append(ch, start, length);
        }

        @Override
        public void endElement(String uri, String localName, String qName) {
            if ("v".equals(qName) || "t".equals(qName)) {
                String value = lastContentsBuilder.toString().trim();
                if (nextIsString) {
                    try {
                        value = sst.getItemAt(Integer.parseInt(value)).getString();
                    } catch (Exception ignored) {}
                }
                if (currentCol >= 0) rowCellMap.put(currentCol, value);
                lastContentsBuilder.setLength(0);
                nextIsString = false;
            } else if ("row".equals(qName)) {
                if (currentRow == 0) {
                    int maxCol = rowCellMap.keySet().stream().max(Integer::compareTo).orElse(-1);
                    headers = new ArrayList<>(maxCol + 1);
                    for (int c = 0; c <= maxCol; c++) headers.add(rowCellMap.getOrDefault(c, ""));
                } else {
                    Map<String, String> rowMap = new LinkedHashMap<>();
                    for (int c = 0; c < headers.size(); c++) rowMap.put(headers.get(c), rowCellMap.getOrDefault(c, ""));

                    final int rowIndex = currentRow;
                    Future<Map<String, String>> fut = exec.submit(() -> handler.process(rowMap, rowIndex));
                    OrderedFuture of = new OrderedFuture(rowIndex, fut, rowMap);
                    try {
                        writeQueue.put(of); // blocks if queue full -> backpressure
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Interrupted while enqueueing write task", e);
                    }
                    submissionIndex++;
                }
            }
        }
    }
}

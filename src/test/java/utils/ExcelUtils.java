package utils;

import org.apache.poi.ss.usermodel.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ExcelUtils - Utilities for reading Excel data using Apache POI.
 *
 * Supported formats: .xlsx and .xls
 */
public class ExcelUtils {

    static {
        // Increase POI in-memory byte array limit (default 100MB) to 256MB
        // Helps with larger XLSX files composed of sizable zip entries
        try {
            org.apache.poi.util.IOUtils.setByteArrayMaxOverride(256 * 1024 * 1024);
        } catch (Throwable ignored) {
            // If POI version doesn't support override, ignore; caller will get the POI exception
        }
    }

    /**
     * Reads the given sheet into a list of maps where each map represents a row,
     * keyed by the header names from the first row.
     */
    public static List<Map<String, String>> readSheetAsListOfMaps(String filePath, String sheetName) {
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }

            List<String> headers = readHeaderRow(sheet);
            List<Map<String, String>> rows = new ArrayList<>();

            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue; // skip empty row

                Map<String, String> rowMap = new HashMap<>();
                for (int c = 0; c < headers.size(); c++) {
                    String header = headers.get(c);
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    rowMap.put(header, getCellStringValue(cell));
                }
                rows.add(rowMap);
            }

            return rows;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel: " + filePath + " | " + e.getMessage(), e);
        }
    }

    /**
     * Reads the given sheet into a two-dimensional Object array suitable for TestNG DataProviders.
     * Skips the header row.
     */
    public static Object[][] readSheetAs2DArray(String filePath, String sheetName) {
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(fis)) {

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }

            Row headerRow = sheet.getRow(sheet.getFirstRowNum());
            if (headerRow == null) {
                return new Object[0][0];
            }

            int columnCount = headerRow.getLastCellNum();

            List<Object[]> data = new ArrayList<>();
            for (int r = sheet.getFirstRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                Object[] rowArr = new Object[columnCount];
                for (int c = 0; c < columnCount; c++) {
                    Cell cell = row.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
                    rowArr[c] = getCellStringValue(cell);
                }
                data.add(rowArr);
            }

            Object[][] result = new Object[data.size()][];
            for (int i = 0; i < data.size(); i++) {
                result[i] = data.get(i);
            }
            return result;
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Excel: " + filePath + " | " + e.getMessage(), e);
        }
    }

    /**
     * Reads a single cell as String. Useful for quick lookups.
     */
    public static String readCell(String filePath, String sheetName, int rowIndex, int colIndex) {
        try (FileInputStream fis = new FileInputStream(new File(filePath));
             Workbook workbook = WorkbookFactory.create(fis)) {
            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }
            Row row = sheet.getRow(rowIndex);
            Cell cell = row != null ? row.getCell(colIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL) : null;
            return getCellStringValue(cell);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read cell: " + filePath + "!" + sheetName + "[" + rowIndex + "," + colIndex + "] | " + e.getMessage(), e);
        }
    }

    /**
     * Writes a String value to the specified cell. Creates the row/cell if missing.
     * Note: rowIndex/colIndex are 0-based.
     */
    public static void writeCell(String filePath, String sheetName, int rowIndex, int colIndex, String value) {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IllegalArgumentException("File not found: " + filePath);
        }
        Workbook workbook = null;
        try {
            // Load workbook then close input BEFORE writing back to same file to avoid corruption
            try (FileInputStream fis = new FileInputStream(file)) {
                workbook = WorkbookFactory.create(fis);
            }

            Sheet sheet = workbook.getSheet(sheetName);
            if (sheet == null) {
                throw new IllegalArgumentException("Sheet not found: " + sheetName);
            }

            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                row = sheet.createRow(rowIndex);
            }

            Cell cell = row.getCell(colIndex, Row.MissingCellPolicy.CREATE_NULL_AS_BLANK);
            cell.setCellValue(value);

            // Write back to file after input stream is closed
            try (FileOutputStream fos = new FileOutputStream(file)) {
                workbook.write(fos);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to write cell: " + filePath + "!" + sheetName + "[" + rowIndex + "," + colIndex + "] | " + e.getMessage(), e);
        } finally {
            if (workbook != null) {
                try { workbook.close(); } catch (IOException ignored) {}
            }
        }
    }

    // ===== Helpers =====
    private static List<String> readHeaderRow(Sheet sheet) {
        Row header = sheet.getRow(sheet.getFirstRowNum());
        if (header == null) return new ArrayList<>();
        List<String> headers = new ArrayList<>();
        short lastCellNum = header.getLastCellNum();
        for (int c = 0; c < lastCellNum; c++) {
            Cell cell = header.getCell(c, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
            headers.add(getCellStringValue(cell));
        }
        return headers;
    }

    public static String getCellStringValue(Cell cell) {
        if (cell == null) return "";

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case BOOLEAN:
                return Boolean.toString(cell.getBooleanCellValue());
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    return df.format(cell.getDateCellValue());
                }
                // Avoid scientific notation; trim trailing .0 for integers
                double num = cell.getNumericCellValue();
                long asLong = (long) num;
                if (asLong == num) {
                    return Long.toString(asLong);
                }
                return Double.toString(num);
            case FORMULA:
                try {
                    FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
                    CellValue evaluated = evaluator.evaluate(cell);
                    if (evaluated == null) return "";
                    switch (evaluated.getCellType()) {
                        case STRING:
                            return evaluated.getStringValue();
                        case BOOLEAN:
                            return Boolean.toString(evaluated.getBooleanValue());
                        case NUMERIC:
                            double fnum = evaluated.getNumberValue();
                            long fasLong = (long) fnum;
                            if (fasLong == fnum) return Long.toString(fasLong);
                            return Double.toString(fnum);
                        default:
                            return "";
                    }
                } catch (Exception e) {
                    return cell.getCellFormula();
                }
            case BLANK:
            case _NONE:
            case ERROR:
            default:
                return "";
        }
    }

    /**
     * Public helper alias for reading a cell's value as String.
     */
    public static String readCellValueAsString(Cell cell) {
        return getCellStringValue(cell);
    }
}



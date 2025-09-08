package utils;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * TestDataReader - Comprehensive data reading utilities
 * 
 * Supports:
 * - CSV files
 * - Excel files (XLS and XLSX)
 * - JSON files
 * - Properties files
 * - Data-driven testing support
 */
public class TestDataReader {
    
    // ====================
    // CSV OPERATIONS
    // ====================
    
    /**
     * Read CSV file and return as List of Maps (header as keys)
     */
    public static List<Map<String, String>> readCsvAsListOfMaps(String csvFilePath) {
        List<Map<String, String>> data = new ArrayList<>();
        
        try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(csvFilePath))
                .withSkipLines(0)
                .build()) {
            
            List<String[]> allData = csvReader.readAll();
            if (allData.isEmpty()) {
                return data;
            }
            
            String[] headers = allData.get(0);
            
            for (int i = 1; i < allData.size(); i++) {
                String[] row = allData.get(i);
                Map<String, String> rowMap = new HashMap<>();
                
                for (int j = 0; j < headers.length && j < row.length; j++) {
                    rowMap.put(headers[j].trim(), row[j].trim());
                }
                data.add(rowMap);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV file: " + csvFilePath, e);
        }
        
        return data;
    }
    
    /**
     * Read CSV file and return as 2D String array
     */
    public static String[][] readCsvAs2DArray(String csvFilePath) {
        try (CSVReader csvReader = new CSVReaderBuilder(new FileReader(csvFilePath))
                .withSkipLines(0)
                .build()) {
            
            List<String[]> allData = csvReader.readAll();
            return allData.toArray(new String[0][]);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to read CSV file: " + csvFilePath, e);
        }
    }
    
    /**
     * Read specific column from CSV file
     */
    public static List<String> readCsvColumn(String csvFilePath, String columnName) {
        List<String> columnData = new ArrayList<>();
        List<Map<String, String>> allData = readCsvAsListOfMaps(csvFilePath);
        
        for (Map<String, String> row : allData) {
            columnData.add(row.get(columnName));
        }
        
        return columnData;
    }
    
    /**
     * Read specific row from CSV file by index
     */
    public static Map<String, String> readCsvRow(String csvFilePath, int rowIndex) {
        List<Map<String, String>> allData = readCsvAsListOfMaps(csvFilePath);
        
        if (rowIndex >= 0 && rowIndex < allData.size()) {
            return allData.get(rowIndex);
        } else {
            throw new IndexOutOfBoundsException("Row index " + rowIndex + " is out of bounds");
        }
    }
    
    // ====================
    // EXCEL OPERATIONS
    // ====================
    
    /**
     * Read Excel file and return as List of Maps (first row as headers)
     */
    public static List<Map<String, String>> readExcelAsListOfMaps(String excelFilePath, String sheetName) {
        List<Map<String, String>> data = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(excelFilePath)) {
            
            Workbook workbook = createWorkbook(excelFilePath, fis);
            Sheet sheet = (sheetName != null) ? workbook.getSheet(sheetName) : workbook.getSheetAt(0);
            
            if (sheet == null) {
                throw new RuntimeException("Sheet not found: " + sheetName);
            }
            
            // Get headers from first row
            Row headerRow = sheet.getRow(0);
            if (headerRow == null) {
                return data;
            }
            
            List<String> headers = new ArrayList<>();
            for (Cell cell : headerRow) {
                headers.add(getCellValueAsString(cell));
            }
            
            // Read data rows
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) continue;
                
                Map<String, String> rowMap = new HashMap<>();
                for (int j = 0; j < headers.size(); j++) {
                    Cell cell = row.getCell(j);
                    String cellValue = (cell != null) ? getCellValueAsString(cell) : "";
                    rowMap.put(headers.get(j), cellValue);
                }
                data.add(rowMap);
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel file: " + excelFilePath, e);
        }
        
        return data;
    }
    
    /**
     * Read Excel file and return as 2D Object array
     */
    public static Object[][] readExcelAs2DArray(String excelFilePath, String sheetName) {
        List<Object[]> data = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(excelFilePath)) {
            
            Workbook workbook = createWorkbook(excelFilePath, fis);
            Sheet sheet = (sheetName != null) ? workbook.getSheet(sheetName) : workbook.getSheetAt(0);
            
            if (sheet == null) {
                throw new RuntimeException("Sheet not found: " + sheetName);
            }
            
            for (Row row : sheet) {
                List<Object> rowData = new ArrayList<>();
                for (Cell cell : row) {
                    rowData.add(getCellValueAsObject(cell));
                }
                data.add(rowData.toArray());
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel file: " + excelFilePath, e);
        }
        
        return data.toArray(new Object[0][]);
    }
    
    /**
     * Get all sheet names from Excel file
     */
    public static List<String> getExcelSheetNames(String excelFilePath) {
        List<String> sheetNames = new ArrayList<>();
        
        try (FileInputStream fis = new FileInputStream(excelFilePath)) {
            
            Workbook workbook = createWorkbook(excelFilePath, fis);
            for (int i = 0; i < workbook.getNumberOfSheets(); i++) {
                sheetNames.add(workbook.getSheetName(i));
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to get sheet names from Excel file: " + excelFilePath, e);
        }
        
        return sheetNames;
    }
    
    /**
     * Read specific cell from Excel file
     */
    public static String readExcelCell(String excelFilePath, String sheetName, int rowIndex, int columnIndex) {
        try (FileInputStream fis = new FileInputStream(excelFilePath)) {
            
            Workbook workbook = createWorkbook(excelFilePath, fis);
            Sheet sheet = (sheetName != null) ? workbook.getSheet(sheetName) : workbook.getSheetAt(0);
            
            if (sheet == null) {
                throw new RuntimeException("Sheet not found: " + sheetName);
            }
            
            Row row = sheet.getRow(rowIndex);
            if (row == null) {
                return "";
            }
            
            Cell cell = row.getCell(columnIndex);
            return (cell != null) ? getCellValueAsString(cell) : "";
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to read Excel cell: " + excelFilePath, e);
        }
    }
    
    // ====================
    // HELPER METHODS
    // ====================
    
    private static Workbook createWorkbook(String filePath, FileInputStream fis) throws IOException {
        if (filePath.endsWith(".xlsx")) {
            return new XSSFWorkbook(fis);
        } else if (filePath.endsWith(".xls")) {
            return new HSSFWorkbook(fis);
        } else {
            throw new RuntimeException("Unsupported file format. Only .xls and .xlsx are supported.");
        }
    }
    
    private static String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return "";
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue().toString();
                } else {
                    // Handle both integer and decimal numbers
                    double numericValue = cell.getNumericCellValue();
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    } else {
                        return String.valueOf(numericValue);
                    }
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
            default:
                return "";
        }
    }
    
    private static Object getCellValueAsObject(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
            default:
                return null;
        }
    }
    
    // ====================
    // JSON OPERATIONS (Delegated to JsonUtils)
    // ====================
    
    /**
     * Read JSON file as List of Maps
     */
    @SuppressWarnings("unchecked")
    public static List<Map<String, Object>> readJsonAsListOfMaps(String jsonFilePath) {
        return (List<Map<String, Object>>) (List<?>) JsonUtils.readJsonFileAsList(jsonFilePath, Map.class);
    }
    
    /**
     * Read JSON file as Map
     */
    public static Map<String, Object> readJsonAsMap(String jsonFilePath) {
        return JsonUtils.readJsonFileAsMap(jsonFilePath);
    }
    
    // ====================
    // PROPERTIES OPERATIONS
    // ====================
    
    /**
     * Read properties file as Map
     */
    public static Map<String, String> readPropertiesAsMap(String propertiesFilePath) {
        Properties properties = new Properties();
        Map<String, String> propertiesMap = new HashMap<>();
        
        try (FileInputStream fis = new FileInputStream(propertiesFilePath)) {
            properties.load(fis);
            
            for (String key : properties.stringPropertyNames()) {
                propertiesMap.put(key, properties.getProperty(key));
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to read properties file: " + propertiesFilePath, e);
        }
        
        return propertiesMap;
    }
    
    // ====================
    // DATA-DRIVEN TESTING SUPPORT
    // ====================
    
    /**
     * Convert List of Maps to TestNG DataProvider format
     */
    public static Object[][] convertToDataProvider(List<Map<String, String>> data) {
        Object[][] dataProvider = new Object[data.size()][1];
        
        for (int i = 0; i < data.size(); i++) {
            dataProvider[i][0] = data.get(i);
        }
        
        return dataProvider;
    }
    
    /**
     * Get test data for specific test case from CSV
     */
    public static Map<String, String> getTestDataFromCsv(String csvFilePath, String testCaseName) {
        List<Map<String, String>> allData = readCsvAsListOfMaps(csvFilePath);
        
        for (Map<String, String> row : allData) {
            if (testCaseName.equals(row.get("TestCase")) || testCaseName.equals(row.get("testcase"))) {
                return row;
            }
        }
        
        throw new RuntimeException("Test case '" + testCaseName + "' not found in CSV file: " + csvFilePath);
    }
    
    /**
     * Get test data for specific test case from Excel
     */
    public static Map<String, String> getTestDataFromExcel(String excelFilePath, String sheetName, String testCaseName) {
        List<Map<String, String>> allData = readExcelAsListOfMaps(excelFilePath, sheetName);
        
        for (Map<String, String> row : allData) {
            if (testCaseName.equals(row.get("TestCase")) || testCaseName.equals(row.get("testcase"))) {
                return row;
            }
        }
        
        throw new RuntimeException("Test case '" + testCaseName + "' not found in Excel file: " + excelFilePath);
    }
}

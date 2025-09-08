package tests;

import base.BaseTest;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import utils.*;

import java.util.List;
import java.util.Map;

/**
 * DataUtilitiesDemo - Comprehensive demonstration of data utilities
 * 
 * This test class demonstrates:
 * - CSV data reading for data-driven testing
 * - JSON configuration loading
 * - Fake data generation
 * - Test data cleanup mechanisms
 * - Database operations (when configured)
 */
public class DataUtilitiesDemo extends BaseTest {
    
    private static final String CSV_TEST_DATA = "src/test/resources/testdata/users.csv";
    private static final String JSON_CONFIG = "src/test/resources/testdata/test-config.json";
    private static final String JSON_ENDPOINTS = "src/test/resources/testdata/api-endpoints.json";
    
    @Test
    public void demonstrateJsonUtils() {
        try {
            ExtentTestManager.logInfo("=== JSON Utils Demonstration ===");
            
            // Read JSON configuration
            Map<String, Object> config = JsonUtils.readJsonFileAsMap(JSON_CONFIG);
            ExtentTestManager.logInfo("Loaded configuration with keys: " + config.keySet());
            
            // Read API endpoints configuration
            Map<String, Object> endpoints = JsonUtils.readJsonFileAsMap(JSON_ENDPOINTS);
            ExtentTestManager.logInfo("Loaded endpoints configuration");
            
            // Extract specific values using JSON path
            String loginUrl = JsonUtils.getValueFromJsonFile(JSON_ENDPOINTS, "endpoints.login.url");
            ExtentTestManager.logInfo("Login URL from JSON: " + loginUrl);
            
            String validMobile = JsonUtils.getValueFromJsonFile(JSON_ENDPOINTS, "testData.validUser.mobile");
            ExtentTestManager.logInfo("Valid mobile from JSON: " + validMobile);
            
            // Create and manipulate JSON
            String originalJson = "{\"name\":\"John\",\"age\":25}";
            String updatedJson = JsonUtils.updateJsonValue(originalJson, "age", 26);
            ExtentTestManager.logInfo("Updated JSON: " + updatedJson);
            
            // Validate JSON
            boolean isValid = JsonUtils.isValidJson(updatedJson);
            ExtentTestManager.logInfo("JSON validation result: " + isValid);
            
            ExtentTestManager.logPass("JSON Utils demonstration completed successfully");
            
        } catch (Exception e) {
            ExtentTestManager.logFail("JSON Utils demonstration failed: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    public void demonstrateDataGenerator() {
        try {
            ExtentTestManager.logInfo("=== Data Generator Demonstration ===");
            
            // Generate personal data
            String fullName = DataGenerator.generateFullName();
            String email = DataGenerator.generateEmail();
            String phone = DataGenerator.generateIndianMobile();
            String password = DataGenerator.generateStrongPassword(8, 12);
            
            ExtentTestManager.logInfo("Generated User Data:");
            ExtentTestManager.logInfo("Name: " + fullName);
            ExtentTestManager.logInfo("Email: " + email);
            ExtentTestManager.logInfo("Phone: " + phone);
            ExtentTestManager.logInfo("Password: " + password);
            
            // Generate financial data
            String amount = DataGenerator.generateAmountString(100, 10000, 2);
            String accountNumber = DataGenerator.generateBankAccountNumber();
            String ifscCode = DataGenerator.generateIfscCode();
            
            ExtentTestManager.logInfo("Generated Financial Data:");
            ExtentTestManager.logInfo("Amount: " + amount);
            ExtentTestManager.logInfo("Account Number: " + accountNumber);
            ExtentTestManager.logInfo("IFSC Code: " + ifscCode);
            
            // Generate dates
            String futureDate = DataGenerator.generateFutureDate(30, "yyyy-MM-dd");
            String pastDate = DataGenerator.generatePastDate(365, "dd/MM/yyyy");
            
            ExtentTestManager.logInfo("Generated Dates:");
            ExtentTestManager.logInfo("Future Date: " + futureDate);
            ExtentTestManager.logInfo("Past Date: " + pastDate);
            
            // Generate random strings and numbers
            String randomString = DataGenerator.generateRandomString(10);
            int randomNumber = DataGenerator.generateRandomInt(1000, 9999);
            String uuid = DataGenerator.generateUUID();
            
            ExtentTestManager.logInfo("Generated Random Data:");
            ExtentTestManager.logInfo("Random String: " + randomString);
            ExtentTestManager.logInfo("Random Number: " + randomNumber);
            ExtentTestManager.logInfo("UUID: " + uuid);
            
            // Generate bulk user data
            List<Map<String, String>> users = DataGenerator.generateMultipleUsers(3);
            ExtentTestManager.logInfo("Generated " + users.size() + " users for bulk testing");
            
            ExtentTestManager.logPass("Data Generator demonstration completed successfully");
            
        } catch (Exception e) {
            ExtentTestManager.logFail("Data Generator demonstration failed: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    public void demonstrateTestDataCleanup() {
        try {
            ExtentTestManager.logInfo("=== Test Data Cleanup Demonstration ===");
            String testName = "cleanup_demo_test";
            
            // Add file cleanup task
            String tempFilePath = System.getProperty("java.io.tmpdir") + "/demo_temp_file.txt";
            TestDataCleanup.addFileCleanup(testName, tempFilePath);
            ExtentTestManager.logInfo("Added file cleanup task for: " + tempFilePath);
            
            // Add custom cleanup task
            TestDataCleanup.addCustomCleanup(testName, "Clear test cache", () -> {
                System.out.println("Clearing test cache...");
            });
            ExtentTestManager.logInfo("Added custom cleanup task");
            
            // Add temporary file cleanup
            TestDataCleanup.addTempFileCleanup(testName);
            ExtentTestManager.logInfo("Added temporary file cleanup task");
            
            // Show registered tasks
            List<TestDataCleanup.CleanupTask> tasks = TestDataCleanup.getCleanupTasks(testName);
            ExtentTestManager.logInfo("Registered " + tasks.size() + " cleanup tasks");
            
            // Execute cleanup
            TestDataCleanup.CleanupResult result = TestDataCleanup.executeCleanup(testName);
            ExtentTestManager.logInfo("Cleanup execution result: " + result.message);
            
            if (result.success) {
                ExtentTestManager.logPass("Test Data Cleanup demonstration completed successfully");
            } else {
                ExtentTestManager.logWarning("Test Data Cleanup completed with some failures");
            }
            
        } catch (Exception e) {
            ExtentTestManager.logFail("Test Data Cleanup demonstration failed: " + e.getMessage());
            throw e;
        }
    }
    
    // Data-driven test using CSV data
    @DataProvider(name = "userTestData")
    public Object[][] getUserTestData() {
        try {
            List<Map<String, String>> csvData = TestDataReader.readCsvAsListOfMaps(CSV_TEST_DATA);
            return TestDataReader.convertToDataProvider(csvData);
        } catch (Exception e) {
            ExtentTestManager.logFail("Failed to read CSV test data: " + e.getMessage());
            return new Object[0][0];
        }
    }
    
    @Test(dataProvider = "userTestData")
    public void demonstrateDataDrivenTesting(Map<String, String> testData) {
        try {
            String testCase = testData.get("TestCase");
            ExtentTestManager.logInfo("=== Data-Driven Test: " + testCase + " ===");
            
            String username = testData.get("Username");
            String password = testData.get("Password");
            String firstName = testData.get("FirstName");
            String lastName = testData.get("LastName");
            String phone = testData.get("Phone");
            String email = testData.get("Email");
            String amount = testData.get("Amount");
            String expected = testData.get("Expected");
            
            ExtentTestManager.logInfo("Test Data:");
            ExtentTestManager.logInfo("Username: " + username);
            ExtentTestManager.logInfo("First Name: " + firstName);
            ExtentTestManager.logInfo("Last Name: " + lastName);
            ExtentTestManager.logInfo("Phone: " + phone);
            ExtentTestManager.logInfo("Email: " + email);
            ExtentTestManager.logInfo("Amount: " + amount);
            ExtentTestManager.logInfo("Expected Result: " + expected);
            
            // Simulate test logic based on expected result
            if ("Success".equals(expected)) {
                ExtentTestManager.logPass("Data-driven test passed for: " + testCase);
            } else {
                ExtentTestManager.logWarning("Data-driven test expecting failure for: " + testCase);
            }
            
            // Add cleanup for this test case
            TestDataCleanup.addCustomCleanup(testCase, "Cleanup after " + testCase, () -> {
                System.out.println("Cleaning up after test case: " + testCase);
            });
            
        } catch (Exception e) {
            ExtentTestManager.logFail("Data-driven test failed: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    public void demonstrateAdvancedJsonOperations() {
        try {
            ExtentTestManager.logInfo("=== Advanced JSON Operations ===");
            
            // Create test user data using DataGenerator
            Map<String, String> userData = DataGenerator.generateUserRegistrationData();
            String userJson = JsonUtils.toJsonString(userData);
            ExtentTestManager.logInfo("Generated user JSON: " + userJson);
            
            // Pretty print JSON
            ExtentTestManager.logInfo("Pretty printed JSON:");
            JsonUtils.prettyPrintJson(userJson);
            
            // Merge JSON objects
            String additionalData = "{\"membership\":\"Gold\",\"joinDate\":\"" + 
                                   DataGenerator.getCurrentDate("yyyy-MM-dd") + "\"}";
            String mergedJson = JsonUtils.mergeJsonObjects(userJson, additionalData);
            ExtentTestManager.logInfo("Merged JSON: " + mergedJson);
            
            // Write to file and read back
            String tempJsonFile = System.getProperty("java.io.tmpdir") + "/test_user_data.json";
            JsonUtils.writeJsonToFile(JsonUtils.parseJsonStringAsMap(mergedJson), tempJsonFile);
            ExtentTestManager.logInfo("Written JSON data to file: " + tempJsonFile);
            
            // Add cleanup for the temporary file
            TestDataCleanup.addFileCleanup("json_operations_test", tempJsonFile);
            
            // Read back and validate
            Map<String, Object> readBackData = JsonUtils.readJsonFileAsMap(tempJsonFile);
            ExtentTestManager.logInfo("Read back data keys: " + readBackData.keySet());
            
            ExtentTestManager.logPass("Advanced JSON Operations demonstration completed successfully");
            
        } catch (Exception e) {
            ExtentTestManager.logFail("Advanced JSON Operations demonstration failed: " + e.getMessage());
            throw e;
        }
    }
    
    @Test
    public void demonstrateTestDataReaderFeatures() {
        try {
            ExtentTestManager.logInfo("=== TestDataReader Features Demonstration ===");
            
            // Read CSV as List of Maps
            List<Map<String, String>> csvData = TestDataReader.readCsvAsListOfMaps(CSV_TEST_DATA);
            ExtentTestManager.logInfo("Read " + csvData.size() + " records from CSV");
            
            // Read specific row
            Map<String, String> specificUser = TestDataReader.readCsvRow(CSV_TEST_DATA, 0);
            ExtentTestManager.logInfo("First user: " + specificUser.get("FirstName") + " " + specificUser.get("LastName"));
            
            // Read specific column
            List<String> usernames = TestDataReader.readCsvColumn(CSV_TEST_DATA, "Username");
            ExtentTestManager.logInfo("All usernames: " + usernames);
            
            // Get test data for specific test case
            Map<String, String> tc001Data = TestDataReader.getTestDataFromCsv(CSV_TEST_DATA, "TC_001");
            ExtentTestManager.logInfo("TC_001 user: " + tc001Data.get("FirstName"));
            
            // Read JSON as Map (since our JSON files are objects, not arrays)
            Map<String, Object> jsonEndpointsData = TestDataReader.readJsonAsMap(JSON_ENDPOINTS);
            ExtentTestManager.logInfo("JSON endpoints data keys: " + jsonEndpointsData.keySet());
            
            Map<String, Object> jsonMapData = TestDataReader.readJsonAsMap(JSON_CONFIG);
            ExtentTestManager.logInfo("Configuration keys: " + jsonMapData.keySet());
            
            ExtentTestManager.logPass("TestDataReader Features demonstration completed successfully");
            
        } catch (Exception e) {
            ExtentTestManager.logFail("TestDataReader Features demonstration failed: " + e.getMessage());
            throw e;
        }
    }
    
    @AfterClass
    public void cleanupAfterAllTests() {
        try {
            System.out.println("=== Final Cleanup ===");
            
            // Execute cleanup for any remaining test cases
            Map<String, TestDataCleanup.CleanupResult> results = TestDataCleanup.executeAllCleanup();
            System.out.println("Executed cleanup for " + results.size() + " test cases");
            
            // Print cleanup summary
            TestDataCleanup.printCleanupSummary();
            
            System.out.println("Final cleanup completed successfully");
            
        } catch (Exception e) {
            System.err.println("Final cleanup failed: " + e.getMessage());
        }
    }
}

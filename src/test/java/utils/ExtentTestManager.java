package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;

import java.util.concurrent.ConcurrentHashMap;

/**
 * ExtentTestManager - Thread-safe ExtentReports test management
 * 
 * Features:
 * - Thread-safe parallel execution support
 * - Automatic test lifecycle management
 * - Enhanced logging with thread information
 * - Graceful error handling for concurrent access
 */
public class ExtentTestManager {
    
    // Thread-safe map for parallel execution
    private static final ConcurrentHashMap<Long, ExtentTest> extentTestMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<Long, String> threadTestNameMap = new ConcurrentHashMap<>();
    
    private static ExtentReports extent = ExtentReportsManager.getInstance();

    /**
     * Get unique thread identifier
     */
    private static long getThreadId() {
        return Thread.currentThread().getId();
    }

    /**
     * Get current test for the current thread
     */
    public static ExtentTest getTest() {
        long threadId = getThreadId();
        ExtentTest test = extentTestMap.get(threadId);
        
        if (test == null) {
            // Fallback: create a temporary test if none exists
            System.err.println("⚠️ No ExtentTest found for thread " + threadId + ". Creating temporary test.");
            test = startTest("UnknownTest_Thread" + threadId);
        }
        
        return test;
    }

    /**
     * End test for current thread
     */
    public static void endTest() {
        long threadId = getThreadId();
        
        // Remove from thread maps
        ExtentTest removedTest = extentTestMap.remove(threadId);
        String removedTestName = threadTestNameMap.remove(threadId);
        
        if (removedTest != null) {
            System.out.println("🧪 [Thread-" + threadId + "] ExtentTest ended: " + 
                             (removedTestName != null ? removedTestName : "Unknown"));
        }
    }

    /**
     * Start test for current thread with name only
     */
    public static synchronized ExtentTest startTest(String testName) {
        long threadId = getThreadId();
        
        // Clean up any existing test for this thread
        endTest();
        
        // Create new test with thread information
        String threadSafeTestName = testName + " [Thread-" + threadId + "]";
        ExtentTest test = extent.createTest(threadSafeTestName);
        
        // Store in thread-safe maps
        extentTestMap.put(threadId, test);
        threadTestNameMap.put(threadId, testName);
        
        System.out.println("🧪 [Thread-" + threadId + "] ExtentTest started: " + testName);
        
        return test;
    }

    /**
     * Start test for current thread with name and description
     */
    public static synchronized ExtentTest startTest(String testName, String desc) {
        long threadId = getThreadId();
        
        // Clean up any existing test for this thread
        endTest();
        
        // Create new test with thread information
        String threadSafeTestName = testName + " [Thread-" + threadId + "]";
        ExtentTest test = extent.createTest(threadSafeTestName, desc);
        
        // Store in thread-safe maps
        extentTestMap.put(threadId, test);
        threadTestNameMap.put(threadId, testName);
        
        System.out.println("🧪 [Thread-" + threadId + "] ExtentTest started: " + testName + " - " + desc);
        
        return test;
    }

    /**
     * Thread-safe logging methods with enhanced error handling
     */
    
    public static void logInfo(String message) {
        try {
            ExtentTest test = getTest();
            if (test != null) {
                test.log(Status.INFO, formatMessageWithThread(message));
            }
        } catch (Exception e) {
            System.out.println("INFO [Thread-" + getThreadId() + "]: " + message);
        }
    }

    public static void logPass(String message) {
        try {
            ExtentTest test = getTest();
            if (test != null) {
                test.log(Status.PASS, formatMessageWithThread(message));
            }
        } catch (Exception e) {
            System.out.println("PASS [Thread-" + getThreadId() + "]: " + message);
        }
    }

    public static void logFail(String message) {
        try {
            ExtentTest test = getTest();
            if (test != null) {
                test.log(Status.FAIL, formatMessageWithThread(message));
            }
        } catch (Exception e) {
            System.err.println("FAIL [Thread-" + getThreadId() + "]: " + message);
        }
    }

    public static void logSkip(String message) {
        try {
            ExtentTest test = getTest();
            if (test != null) {
                test.log(Status.SKIP, formatMessageWithThread(message));
            }
        } catch (Exception e) {
            System.out.println("SKIP [Thread-" + getThreadId() + "]: " + message);
        }
    }

    public static void logWarning(String message) {
        try {
            ExtentTest test = getTest();
            if (test != null) {
                test.log(Status.WARNING, formatMessageWithThread(message));
            }
        } catch (Exception e) {
            System.err.println("WARNING [Thread-" + getThreadId() + "]: " + message);
        }
    }

    public static void logRequest(String endpoint, String method, String requestBody) {
        try {
            ExtentTest test = getTest();
            if (test != null) {
                test.info("<b>API Request Details:</b>");
                test.info("Endpoint: " + endpoint);
                test.info("Method: " + method);
                if (requestBody != null && !requestBody.isEmpty()) {
                    test.info("Request Body: <pre>" + requestBody + "</pre>");
                }
            }
        } catch (Exception e) {
            System.out.println("REQUEST [Thread-" + getThreadId() + "]: " + method + " " + endpoint);
        }
    }

    public static void logResponse(int statusCode, String responseBody, long responseTime) {
        try {
            ExtentTest test = getTest();
            if (test != null) {
                test.info("<b>API Response Details:</b>");
                test.info("Status Code: " + statusCode);
                test.info("Response Time: " + responseTime + " ms");
                if (responseBody != null && !responseBody.isEmpty()) {
                    test.info("Response Body: <pre>" + responseBody + "</pre>");
                }
            }
        } catch (Exception e) {
            System.out.println("RESPONSE [Thread-" + getThreadId() + "]: " + statusCode + " (" + responseTime + "ms)");
        }
    }

    public static void logHeaders(java.util.Map<String, String> headers) {
        try {
            ExtentTest test = getTest();
            if (test != null && headers != null && !headers.isEmpty()) {
                test.info("<b>Headers:</b>");
                headers.forEach((key, value) -> test.info(key + ": " + value));
            }
        } catch (Exception e) {
            System.out.println("HEADERS [Thread-" + getThreadId() + "]: " + (headers != null ? headers.size() + " headers" : "No headers"));
        }
    }

    public static void addCategory(String category) {
        try {
            ExtentTest test = getTest();
            if (test != null) {
                test.assignCategory(category);
            }
        } catch (Exception e) {
            System.out.println("CATEGORY [Thread-" + getThreadId() + "]: " + category);
        }
    }

    public static void addAuthor(String author) {
        try {
            ExtentTest test = getTest();
            if (test != null) {
                test.assignAuthor(author);
            }
        } catch (Exception e) {
            System.out.println("AUTHOR [Thread-" + getThreadId() + "]: " + author);
        }
    }
    
    // ====================
    // UTILITY METHODS
    // ====================
    
    /**
     * Format message with thread information for better parallel execution tracking
     */
    private static String formatMessageWithThread(String message) {
        return message; // Thread info is already in test name, keep message clean
    }
    
    /**
     * Get current thread's test name
     */
    public static String getCurrentTestName() {
        return threadTestNameMap.get(getThreadId());
    }
    
    /**
     * Check if current thread has an active test
     */
    public static boolean hasActiveTest() {
        return extentTestMap.containsKey(getThreadId());
    }
    
    /**
     * Get total number of active tests across all threads
     */
    public static int getActiveTestCount() {
        return extentTestMap.size();
    }
    
    /**
     * Get all active thread IDs with tests
     */
    public static java.util.Set<Long> getActiveThreadIds() {
        return extentTestMap.keySet();
    }
    
    /**
     * Force cleanup for thread (emergency cleanup)
     */
    public static void forceCleanupThread(long threadId) {
        extentTestMap.remove(threadId);
        threadTestNameMap.remove(threadId);
        System.out.println("🧹 [Thread-" + threadId + "] Force cleanup completed");
    }
    
    /**
     * Get thread-safe summary of current state
     */
    public static String getThreadSummary() {
        return String.format("ExtentTestManager: %d active tests across threads %s", 
                           getActiveTestCount(), 
                           getActiveThreadIds().toString());
    }
}

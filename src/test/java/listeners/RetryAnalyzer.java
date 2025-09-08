package listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;
import utils.ExtentTestManager;

/**
 * RetryAnalyzer - Automatic retry mechanism for flaky tests
 * 
 * Features:
 * - Configurable retry count
 * - Detailed retry logging
 * - ExtentReports integration
 * - Thread-safe implementation
 * - Conditional retry based on failure type
 */
public class RetryAnalyzer implements IRetryAnalyzer {
    
    private int retryCount = 0;
    private static final int MAX_RETRY_COUNT = getMaxRetryCount();
    
    // Configuration via system properties
    private static final String RETRY_COUNT_PROPERTY = "test.retry.count";
    private static final String RETRY_ENABLED_PROPERTY = "test.retry.enabled";
    
    @Override
    public boolean retry(ITestResult result) {
        // Check if retry is enabled
        if (!isRetryEnabled()) {
            return false;
        }
        
        // Check if we haven't exceeded max retry count
        if (retryCount < MAX_RETRY_COUNT) {
            retryCount++;
            
            // Log retry attempt
            logRetryAttempt(result);
            
            // Check if this failure type should be retried
            if (shouldRetry(result)) {
                System.out.println("🔄 Retrying test: " + result.getMethod().getMethodName() + 
                                 " (Attempt " + (retryCount + 1) + "/" + (MAX_RETRY_COUNT + 1) + ")");
                return true;
            }
        }
        
        // Log final failure if all retries exhausted
        if (retryCount >= MAX_RETRY_COUNT) {
            logFinalFailure(result);
        }
        
        return false;
    }
    
    /**
     * Determine if retry is enabled via system property
     */
    private static boolean isRetryEnabled() {
        String retryEnabled = System.getProperty(RETRY_ENABLED_PROPERTY, "true");
        return Boolean.parseBoolean(retryEnabled);
    }
    
    /**
     * Get maximum retry count from system property
     */
    private static int getMaxRetryCount() {
        String maxRetry = System.getProperty(RETRY_COUNT_PROPERTY, "2");
        try {
            int count = Integer.parseInt(maxRetry);
            return Math.max(0, Math.min(count, 5)); // Limit between 0-5 retries
        } catch (NumberFormatException e) {
            System.err.println("Invalid retry count format, using default: 2");
            return 2;
        }
    }
    
    /**
     * Determine if the failure should be retried based on failure type
     */
    private boolean shouldRetry(ITestResult result) {
        Throwable throwable = result.getThrowable();
        
        if (throwable == null) {
            return false;
        }
        
        String errorMessage = throwable.getMessage();
        String errorClass = throwable.getClass().getSimpleName();
        
        // Don't retry assertion failures (these are likely genuine test failures)
        if (errorClass.contains("AssertionError") || errorClass.contains("AssertionException")) {
            System.out.println("❌ Not retrying - Assertion failure detected");
            return false;
        }
        
        // Retry on infrastructure/network failures
        if (shouldRetryOnError(errorMessage, errorClass)) {
            return true;
        }
        
        // Don't retry compilation errors or configuration failures
        if (errorClass.contains("TestNGException") || errorClass.contains("ConfigurationException")) {
            System.out.println("❌ Not retrying - Configuration/Setup failure detected");
            return false;
        }
        
        // Default: retry unknown failures
        return true;
    }
    
    /**
     * Check if error message/class indicates a retryable failure
     */
    private boolean shouldRetryOnError(String errorMessage, String errorClass) {
        if (errorMessage == null) {
            return true;
        }
        
        String lowerErrorMessage = errorMessage.toLowerCase();
        
        // Network/Infrastructure failures - should retry
        return lowerErrorMessage.contains("timeout") ||
               lowerErrorMessage.contains("connection") ||
               lowerErrorMessage.contains("socket") ||
               lowerErrorMessage.contains("network") ||
               lowerErrorMessage.contains("502") ||
               lowerErrorMessage.contains("503") ||
               lowerErrorMessage.contains("504") ||
               lowerErrorMessage.contains("500") ||
               errorClass.contains("ConnectException") ||
               errorClass.contains("SocketTimeoutException") ||
               errorClass.contains("IOException");
    }
    
    /**
     * Log retry attempt with details
     */
    private void logRetryAttempt(ITestResult result) {
        try {
            String methodName = result.getMethod().getMethodName();
            String errorMessage = result.getThrowable() != null ? 
                                result.getThrowable().getMessage() : "Unknown error";
            
            String logMessage = String.format(
                "🔄 Retry attempt %d/%d for test: %s\n" +
                "Failure reason: %s\n" +
                "Retry triggered due to: %s",
                retryCount, MAX_RETRY_COUNT, methodName, 
                errorMessage, getRetryReason(result.getThrowable())
            );
            
            System.out.println(logMessage);
            
            // Log to ExtentReports if available (but handle gracefully if test context is lost)
            try {
                if (ExtentTestManager.getTest() != null) {
                    ExtentTestManager.logWarning("Test failed - Attempting retry " + retryCount + "/" + MAX_RETRY_COUNT);
                    ExtentTestManager.logInfo("Failure reason: " + errorMessage);
                }
            } catch (Exception e) {
                // ExtentTest might not be available during retry - that's okay
                System.out.println("Note: ExtentReports logging not available during retry");
            }
            
        } catch (Exception e) {
            System.err.println("Error logging retry attempt: " + e.getMessage());
        }
    }
    
    /**
     * Log final failure after all retries exhausted
     */
    private void logFinalFailure(ITestResult result) {
        try {
            String methodName = result.getMethod().getMethodName();
            String errorMessage = result.getThrowable() != null ? 
                                result.getThrowable().getMessage() : "Unknown error";
            
            String logMessage = String.format(
                "❌ Test failed after %d retry attempts: %s\n" +
                "Final failure reason: %s",
                MAX_RETRY_COUNT, methodName, errorMessage
            );
            
            System.err.println(logMessage);
            
            // Log to ExtentReports if available
            try {
                if (ExtentTestManager.getTest() != null) {
                    ExtentTestManager.logFail("Test failed after " + MAX_RETRY_COUNT + " retry attempts");
                    ExtentTestManager.logFail("Final failure: " + errorMessage);
                }
            } catch (Exception e) {
                // Handle gracefully
                System.out.println("Note: ExtentReports logging not available for final failure");
            }
            
        } catch (Exception e) {
            System.err.println("Error logging final failure: " + e.getMessage());
        }
    }
    
    /**
     * Get human-readable retry reason
     */
    private String getRetryReason(Throwable throwable) {
        if (throwable == null) {
            return "Unknown failure";
        }
        
        String errorClass = throwable.getClass().getSimpleName();
        String errorMessage = throwable.getMessage();
        
        if (errorMessage != null) {
            String lowerMessage = errorMessage.toLowerCase();
            if (lowerMessage.contains("timeout")) {
                return "Network timeout detected";
            } else if (lowerMessage.contains("connection")) {
                return "Connection issue detected";
            } else if (lowerMessage.contains("50")) {
                return "Server error detected";
            }
        }
        
        if (errorClass.contains("IOException")) {
            return "I/O operation failure";
        } else if (errorClass.contains("Exception")) {
            return "Infrastructure failure";
        }
        
        return "Transient failure detected";
    }
    
    /**
     * Reset retry count (called between test methods)
     */
    public void resetRetryCount() {
        this.retryCount = 0;
    }
    
    /**
     * Get current retry count
     */
    public int getCurrentRetryCount() {
        return retryCount;
    }
    
    /**
     * Get max retry count
     */
    public static int getMaxRetries() {
        return MAX_RETRY_COUNT;
    }
    
    /**
     * Check if retries are enabled
     */
    public static boolean areRetriesEnabled() {
        return isRetryEnabled();
    }
}

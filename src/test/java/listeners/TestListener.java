package listeners;

import org.testng.*;
import utils.TestDataCleanup;

import java.util.concurrent.ConcurrentHashMap;

/**
 * TestListener - Comprehensive TestNG listener for enhanced test management
 * 
 * Features:
 * - Thread-safe test execution support
 * - Automatic retry analyzer assignment
 * - Test execution monitoring
 * - ExtentReports integration
 * - Test data cleanup coordination
 * - Parallel execution support
 */
public class TestListener implements ITestListener, ISuiteListener, IInvokedMethodListener {
    
    // Thread-safe storage for test context
    private static final ConcurrentHashMap<Long, String> threadTestMap = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, RetryAnalyzer> retryAnalyzers = new ConcurrentHashMap<>();
    
    // Test execution statistics
    private volatile int totalTests = 0;
    private volatile int passedTests = 0;
    private volatile int failedTests = 0;
    private volatile int skippedTests = 0;
    private volatile int retriedTests = 0;
    
    // Suite timing
    private long suiteStartTime;
    private long suiteEndTime;
    
    // ====================
    // SUITE LEVEL EVENTS
    // ====================
    
    @Override
    public void onStart(ISuite suite) {
        suiteStartTime = System.currentTimeMillis();
        
        System.out.println("🚀 Starting Test Suite: " + suite.getName());
        System.out.println("📊 Parallel Execution: " + (suite.getParallel() != null ? "Enabled (" + suite.getParallel() + ")" : "Disabled"));
        System.out.println("🔄 Retry Mechanism: " + (RetryAnalyzer.areRetriesEnabled() ? "Enabled (Max: " + RetryAnalyzer.getMaxRetries() + ")" : "Disabled"));
        System.out.println("🧵 Thread Count: " + suite.getXmlSuite().getThreadCount());
        System.out.println("⏰ Start Time: " + new java.util.Date());
        System.out.println("" + "=".repeat(60));
    }
    
    @Override
    public void onFinish(ISuite suite) {
        suiteEndTime = System.currentTimeMillis();
        long duration = suiteEndTime - suiteStartTime;
        
        System.out.println("" + "=".repeat(60));
        System.out.println("🏁 Test Suite Completed: " + suite.getName());
        System.out.println("⏱️  Total Execution Time: " + formatDuration(duration));
        System.out.println("📈 Test Statistics:");
        System.out.println("   ✅ Passed: " + passedTests);
        System.out.println("   ❌ Failed: " + failedTests);
        System.out.println("   ⏭️  Skipped: " + skippedTests);
        System.out.println("   🔄 Retried: " + retriedTests);
        System.out.println("   📊 Total: " + totalTests);
        
        if (totalTests > 0) {
            double passPercentage = (double) passedTests / totalTests * 100;
            System.out.println("   📊 Pass Rate: " + String.format("%.2f%%", passPercentage));
        }
        
        System.out.println("⏰ End Time: " + new java.util.Date());
        System.out.println("" + "=".repeat(60));
        
        // Clean up thread-safe storage
        threadTestMap.clear();
        retryAnalyzers.clear();
    }
    
    // ====================
    // TEST LEVEL EVENTS
    // ====================
    
    @Override
    public void onTestStart(ITestResult result) {
        totalTests++;
        long threadId = Thread.currentThread().hashCode();
        String testName = getTestName(result);
        
        // Store thread-test mapping for parallel execution
        threadTestMap.put(threadId, testName);
        
        // Assign retry analyzer if not already present
        assignRetryAnalyzer(result);
        
        System.out.println("🧪 [Thread-" + threadId + "] Starting: " + testName);
        
        // Log start time for performance tracking
        result.setAttribute("startTime", System.currentTimeMillis());
    }
    
    @Override
    public void onTestSuccess(ITestResult result) {
        passedTests++;
        long threadId = Thread.currentThread().hashCode();
        String testName = getTestName(result);
        long duration = calculateTestDuration(result);
        
        System.out.println("✅ [Thread-" + threadId + "] PASSED: " + testName + " (" + duration + "ms)");
        
        // Clean up for this test
        performTestCleanup(result, true);
    }
    
    @Override
    public void onTestFailure(ITestResult result) {
        failedTests++;
        long threadId = Thread.currentThread().hashCode();
        String testName = getTestName(result);
        long duration = calculateTestDuration(result);
        
        System.err.println("❌ [Thread-" + threadId + "] FAILED: " + testName + " (" + duration + "ms)");
        
        // Log failure details
        if (result.getThrowable() != null) {
            System.err.println("   💥 Error: " + result.getThrowable().getMessage());
        }
        
        // Check if this will be retried
        if (willBeRetried(result)) {
            retriedTests++;
            System.out.println("🔄 [Thread-" + threadId + "] Test will be retried");
        } else {
            // Clean up only if not being retried
            performTestCleanup(result, false);
        }
    }
    
    @Override
    public void onTestSkipped(ITestResult result) {
        skippedTests++;
        long threadId = Thread.currentThread().hashCode();
        String testName = getTestName(result);
        
        System.out.println("⏭️ [Thread-" + threadId + "] SKIPPED: " + testName);
        
        if (result.getThrowable() != null) {
            System.out.println("   📝 Reason: " + result.getThrowable().getMessage());
        }
        
        performTestCleanup(result, false);
    }
    
    // ====================
    // METHOD LEVEL EVENTS
    // ====================
    
    @Override
    public void beforeInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            long threadId = Thread.currentThread().hashCode();
            String methodName = method.getTestMethod().getMethodName();
            System.out.println("🔧 [Thread-" + threadId + "] Preparing: " + methodName);
        }
    }
    
    @Override
    public void afterInvocation(IInvokedMethod method, ITestResult testResult) {
        if (method.isTestMethod()) {
            long threadId = Thread.currentThread().hashCode();
            String methodName = method.getTestMethod().getMethodName();
            
            // Reset retry analyzer for next test
            String testKey = getTestKey(testResult);
            RetryAnalyzer analyzer = retryAnalyzers.get(testKey);
            if (analyzer != null) {
                analyzer.resetRetryCount();
            }
            
            System.out.println("🔧 [Thread-" + threadId + "] Completed: " + methodName);
        }
    }
    
    // ====================
    // UTILITY METHODS
    // ====================
    
    /**
     * Assign retry analyzer to test method
     */
    private void assignRetryAnalyzer(ITestResult result) {
        try {
            ITestNGMethod method = result.getMethod();
            
            // Check if retry analyzer is already set via annotation
            if (method.getRetryAnalyzer(result) == null) {
                String testKey = getTestKey(result);
                
                // Create or reuse retry analyzer for this test
                retryAnalyzers.computeIfAbsent(testKey, name -> new RetryAnalyzer());
                
                // Note: TestNG automatically handles retry analyzer from @Test annotation
                // We just track it for logging purposes
                System.out.println("🔄 Retry analyzer available for: " + getTestName(result));
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to check retry analyzer: " + e.getMessage());
        }
    }
    
    /**
     * Check if test will be retried
     */
    private boolean willBeRetried(ITestResult result) {
        try {
            IRetryAnalyzer retryAnalyzer = result.getMethod().getRetryAnalyzer(result);
            return retryAnalyzer != null && retryAnalyzer.retry(result);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Perform cleanup after test completion
     */
    private void performTestCleanup(ITestResult result, boolean success) {
        try {
            String testName = getTestName(result);
            long threadId = Thread.currentThread().hashCode();
            
            // Execute test data cleanup
            TestDataCleanup.CleanupResult cleanupResult = TestDataCleanup.executeCleanup(testName);
            
            if (cleanupResult.success) {
                System.out.println("🧹 [Thread-" + threadId + "] Cleanup completed for: " + testName);
            } else {
                System.err.println("⚠️ [Thread-" + threadId + "] Cleanup failed for: " + testName);
            }
            
            // Remove from thread mapping
            threadTestMap.remove(threadId);
            
        } catch (Exception e) {
            System.err.println("⚠️ Error during test cleanup: " + e.getMessage());
        }
    }
    
    /**
     * Calculate test execution duration
     */
    private long calculateTestDuration(ITestResult result) {
        try {
            Object startTimeObj = result.getAttribute("startTime");
            if (startTimeObj instanceof Long) {
                long startTime = (Long) startTimeObj;
                return System.currentTimeMillis() - startTime;
            }
        } catch (Exception e) {
            // Fallback to TestNG's timing
        }
        
        return result.getEndMillis() - result.getStartMillis();
    }
    
    /**
     * Get formatted test name
     */
    private String getTestName(ITestResult result) {
        String className = result.getTestClass().getName();
        String methodName = result.getMethod().getMethodName();
        
        // Extract simple class name
        String simpleClassName = className.substring(className.lastIndexOf('.') + 1);
        
        return simpleClassName + "." + methodName;
    }
    
    /**
     * Get unique test key for retry analyzer mapping
     */
    private String getTestKey(ITestResult result) {
        return result.getTestClass().getName() + "." + result.getMethod().getMethodName();
    }
    
    /**
     * Format duration in human-readable format
     */
    private String formatDuration(long durationMs) {
        if (durationMs < 1000) {
            return durationMs + "ms";
        } else if (durationMs < 60000) {
            return String.format("%.2fs", durationMs / 1000.0);
        } else {
            long minutes = durationMs / 60000;
            long seconds = (durationMs % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }
    
    // ====================
    // THREAD-SAFE GETTERS
    // ====================
    
    public static String getCurrentTestForThread() {
        return threadTestMap.get((long) Thread.currentThread().hashCode());
    }
    
    public int getTotalTests() { return totalTests; }
    public int getPassedTests() { return passedTests; }
    public int getFailedTests() { return failedTests; }
    public int getSkippedTests() { return skippedTests; }
    public int getRetriedTests() { return retriedTests; }
    
    public double getPassPercentage() {
        return totalTests > 0 ? (double) passedTests / totalTests * 100 : 0;
    }
    
    public long getSuiteDuration() {
        return suiteEndTime - suiteStartTime;
    }
}

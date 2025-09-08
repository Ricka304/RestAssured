package tests;

import base.BaseTest;
import listeners.RetryAnalyzer;
import org.testng.Assert;
import org.testng.annotations.Test;
import utils.DataGenerator;
import utils.ExtentTestManager;

import java.util.Random;

/**
 * ParallelRetryDemo - Demonstration of parallel execution and retry mechanisms
 * 
 * This class contains various test scenarios to demonstrate:
 * - Parallel test execution
 * - Retry mechanisms for flaky tests
 * - Thread-safe reporting
 * - Different failure scenarios
 */
public class ParallelRetryDemo extends BaseTest {
    
    private static final Random random = new Random();
    
    /**
     * Test that always passes - for parallel execution demo
     */
    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void testAlwaysPass() {
        try {
            ExtentTestManager.logInfo("This test always passes");
            
            // Simulate some work
            try {
                Thread.sleep(1000 + random.nextInt(2000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
            
            String randomData = DataGenerator.generateRandomString(10);
            ExtentTestManager.logInfo("Generated random data: " + randomData);
            
            Assert.assertTrue(true, "This assertion always passes");
            ExtentTestManager.logPass("Test completed successfully");
            
        } catch (Exception e) {
            ExtentTestManager.logFail("Unexpected error: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Test that sometimes fails - demonstrates retry mechanism
     */
    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void testSometimesFails() {
        try {
            ExtentTestManager.logInfo("This test has a 60% failure rate to demonstrate retry");
            
            // Simulate some work
            try {
                Thread.sleep(500 + random.nextInt(1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
            
            // 60% chance of failure to demonstrate retry
            boolean shouldFail = random.nextDouble() < 0.6;
            
            ExtentTestManager.logInfo("Random outcome determination: " + (shouldFail ? "FAIL" : "PASS"));
            
            if (shouldFail) {
                ExtentTestManager.logWarning("Simulating a transient failure (network timeout)");
                throw new RuntimeException("Simulated network timeout - this should trigger retry");
            }
            
            ExtentTestManager.logPass("Test passed on this attempt");
            Assert.assertTrue(true);
            
        } catch (RuntimeException e) {
            ExtentTestManager.logFail("Test failed: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ExtentTestManager.logFail("Unexpected error: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Test with assertion error - should NOT be retried
     */
    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void testWithAssertionError() {
        try {
            ExtentTestManager.logInfo("This test fails with assertion error - should NOT be retried");
            
            // Simulate some work
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
            
            String actualValue = "actual";
            String expectedValue = "expected";
            
            ExtentTestManager.logInfo("Comparing values: actual='" + actualValue + "', expected='" + expectedValue + "'");
            
            // This will always fail with AssertionError
            Assert.assertEquals(actualValue, expectedValue, "Values should match");
            
        } catch (AssertionError e) {
            ExtentTestManager.logFail("Assertion failed (this should NOT be retried): " + e.getMessage());
            throw e;
        } catch (Exception e) {
            ExtentTestManager.logFail("Unexpected error: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Test simulating network issues - should be retried
     */
    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void testNetworkIssues() {
        try {
            ExtentTestManager.logInfo("Simulating network connectivity issues");
            
            // Simulate network delay
            try {
                Thread.sleep(200 + random.nextInt(500));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
            
            // 70% chance of network failure
            boolean networkFailure = random.nextDouble() < 0.7;
            
            if (networkFailure) {
                String[] networkErrors = {
                    "Connection timeout",
                    "Socket timeout exception", 
                    "Network unreachable",
                    "HTTP 502 Bad Gateway",
                    "HTTP 503 Service Unavailable"
                };
                
                String error = networkErrors[random.nextInt(networkErrors.length)];
                ExtentTestManager.logWarning("Network issue detected: " + error);
                throw new RuntimeException(error);
            }
            
            ExtentTestManager.logPass("Network connectivity successful");
            Assert.assertTrue(true);
            
        } catch (Exception e) {
            ExtentTestManager.logFail("Network test failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Fast test for parallel execution demonstration
     */
    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void testFastExecution() {
        try {
            ExtentTestManager.logInfo("Fast execution test - should complete quickly");
            
            // Quick execution
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
            
            int randomNumber = DataGenerator.generateRandomInt(1, 100);
            ExtentTestManager.logInfo("Generated random number: " + randomNumber);
            
            Assert.assertTrue(randomNumber > 0 && randomNumber <= 100);
            ExtentTestManager.logPass("Fast test completed");
            
        } catch (Exception e) {
            ExtentTestManager.logFail("Fast test failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Slow test for parallel execution demonstration
     */
    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void testSlowExecution() {
        try {
            ExtentTestManager.logInfo("Slow execution test - simulating complex operations");
            
            // Simulate slow operation
            try {
                Thread.sleep(3000 + random.nextInt(2000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
            
            String userData = DataGenerator.generateEmail();
            ExtentTestManager.logInfo("Generated user data: " + userData);
            
            Assert.assertNotNull(userData);
            ExtentTestManager.logPass("Slow test completed successfully");
            
        } catch (Exception e) {
            ExtentTestManager.logFail("Slow test failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Test demonstrating thread safety
     */
    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void testThreadSafety() {
        try {
            long threadId = Thread.currentThread().hashCode();
            ExtentTestManager.logInfo("Testing thread safety - Thread ID: " + threadId);
            
            // Simulate concurrent operations
            try {
                Thread.sleep(500 + random.nextInt(1000));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
            
            // Generate thread-specific data
            String threadData = "Thread-" + threadId + "-Data-" + DataGenerator.generateRandomString(5);
            ExtentTestManager.logInfo("Thread-specific data: " + threadData);
            
            // Verify thread-specific data integrity
            Assert.assertTrue(threadData.contains("Thread-" + threadId));
            ExtentTestManager.logPass("Thread safety test passed");
            
        } catch (Exception e) {
            ExtentTestManager.logFail("Thread safety test failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Test that always fails after retries (for demo)
     */
    @Test(retryAnalyzer = RetryAnalyzer.class, enabled = false) // Disabled by default
    public void testAlwaysFails() {
        try {
            ExtentTestManager.logInfo("This test always fails to demonstrate retry exhaustion");
            
            // Simulate some work
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
            
            ExtentTestManager.logWarning("Simulating persistent failure");
            throw new RuntimeException("Persistent connection failure - will exhaust all retries");
            
        } catch (Exception e) {
            ExtentTestManager.logFail("Test failed: " + e.getMessage());
            throw e;
        }
    }
    
    /**
     * Performance test with timing assertions
     */
    @Test(retryAnalyzer = RetryAnalyzer.class)
    public void testPerformance() {
        try {
            ExtentTestManager.logInfo("Performance test - verifying execution time");
            
            long startTime = System.currentTimeMillis();
            
            // Simulate operation that should complete within 2 seconds
            try {
                Thread.sleep(800 + random.nextInt(400));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new RuntimeException("Thread interrupted", e);
            }
            
            long endTime = System.currentTimeMillis();
            long duration = endTime - startTime;
            
            ExtentTestManager.logInfo("Operation completed in: " + duration + "ms");
            
            // Assert performance requirement
            Assert.assertTrue(duration < 2000, "Operation should complete within 2 seconds");
            ExtentTestManager.logPass("Performance test passed - Duration: " + duration + "ms");
            
        } catch (Exception e) {
            ExtentTestManager.logFail("Performance test failed: " + e.getMessage());
            throw e;
        }
    }
}

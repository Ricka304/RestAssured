package listeners;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * Simple retry analyzer controlled by TestNG suite parameters:
 * - retry.enabled (true/false)
 * - retry.count (int)
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final boolean retriesEnabled;
    private static final int maxRetries;

    static {
        String enabledStr = System.getProperty("retry.enabled");
        if (enabledStr == null) enabledStr = System.getProperty("surefire.test.args.retry.enabled");
        if (enabledStr == null) enabledStr = System.getenv("RETRY_ENABLED");
        if (enabledStr == null) enabledStr = "true"; // default

        String countStr = System.getProperty("retry.count");
        if (countStr == null) countStr = System.getProperty("surefire.test.args.retry.count");
        if (countStr == null) countStr = System.getenv("RETRY_COUNT");
        if (countStr == null) countStr = "2"; // default

        retriesEnabled = Boolean.parseBoolean(enabledStr);
        int parsed;
        try { parsed = Integer.parseInt(countStr); } catch (Exception e) { parsed = 2; }
        maxRetries = Math.max(0, parsed);
    }

    private int currentRetry = 0;

    @Override
    public boolean retry(ITestResult result) {
        // Do not retry assertion errors
        if (result.getThrowable() instanceof AssertionError) {
            return false;
        }
        if (!retriesEnabled) return false;
        if (currentRetry < maxRetries) {
            currentRetry++;
            return true;
        }
        return false;
    }

    public void resetRetryCount() {
        currentRetry = 0;
    }

    public static boolean areRetriesEnabled() { return retriesEnabled; }
    public static int getMaxRetries() { return maxRetries; }
}



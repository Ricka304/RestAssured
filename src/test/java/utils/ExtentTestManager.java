package utils;


import com.aventstack.extentreports.ExtentTest;


/**
 * Thread-safe ExtentTest manager and logging helpers.
 */
public class ExtentTestManager {


    private static final ThreadLocal<ExtentTest> currentTest = new ThreadLocal<>();


    public static void startTest(String testName) {
        ExtentTest test = ExtentReportsManager.getInstance().createTest(testName);
        currentTest.set(test);
    }


    public static void endTest() {
        currentTest.remove();
    }
    public static ExtentTest getTest() {
        ExtentTest test = currentTest.get();
        if (test == null) {
// Fail fast so we find ordering issues early
            throw new IllegalStateException("ExtentTest is null. Did you register the ExtentTestNGListener? Logging occurred before test was created.");
        }
        return test;
    }


    public static void addCategory(String category) {
        getTest().assignCategory(category);
    }


    public static void addAuthor(String author) {
        getTest().assignAuthor(author);
    }


    public static void logInfo(String message) {
        getTest().info(message);
    }
    public static void logPass(String message) {
        getTest().pass(message);
    }


    public static void logFail(String message) {
        getTest().fail(message);
    }


    public static void logFail(Throwable t) {
        if (t == null) {
            getTest().fail("Test failed with no throwable");
        } else {
            getTest().fail(t);
        }
    }


    public static void logSkip(String message) {
        getTest().skip(message);
    }


    public static void logWarning(String message) {
        getTest().warning(message);
    }
    // ==== HTTP logging helpers used by RequestResponseLogger ====
    public static void logRequest(String uri, String method, String body) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>Request:</b> ").append(method).append(" ").append(uri);
        if (body != null && !body.isEmpty()) {
            sb.append("<br/><b>Body:</b><pre>").append(escapeHtml(body)).append("</pre>");
        }
        getTest().info(sb.toString());
    }


    public static void logHeaders(java.util.Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) return;
        StringBuilder sb = new StringBuilder("<b>Headers:</b><br/>");
        headers.forEach((k, v) -> sb.append(k).append(": ").append(v).append("<br/>"));
        getTest().info(sb.toString());
    }
    public static void logResponse(int statusCode, String body, long responseTimeMs) {
        StringBuilder sb = new StringBuilder();
        sb.append("<b>Response:</b> status=").append(statusCode)
                .append(", time=").append(responseTimeMs).append("ms");
        if (body != null && !body.isEmpty()) {
            sb.append("<br/><b>Body:</b><pre>").append(escapeHtml(body)).append("</pre>");
        }
        getTest().info(sb.toString());
    }


    private static String escapeHtml(String s) {
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}

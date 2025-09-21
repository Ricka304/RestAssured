package context;


import io.restassured.specification.RequestSpecification;


import java.util.HashMap;
import java.util.Map;


/**
 * Per-thread test context. Holds runtime/test-scoped state such as mock base URLs
 * and a per-thread RequestSpecification. Remember to call TestContext.remove() in
 * @AfterMethod to avoid ThreadLocal leaks.
 */
public final class TestContext {


    private static final ThreadLocal<TestContext> CTX = ThreadLocal.withInitial(TestContext::new);


    private String digilockerBaseUrl;
    private String paymentBaseUrl;
    private String jioSignBaseUrl;
    private RequestSpecification requestSpecification; // optional helper
    private final Map<String, Object> extras = new HashMap<>();


    private TestContext() {}


    public static TestContext get() { return CTX.get(); }


    /** Remove thread-local context. Call this in @AfterMethod to avoid leaks. */
    public static void remove() { CTX.remove(); }


    // --- getters / setters ---
    public String getDigilockerBaseUrl() { return digilockerBaseUrl; }
    public void setDigilockerBaseUrl(String url) { this.digilockerBaseUrl = url; }


    public String getPaymentBaseUrl() { return paymentBaseUrl; }
    public void setPaymentBaseUrl(String url) { this.paymentBaseUrl = url; }


    public String getJioSignBaseUrl() { return jioSignBaseUrl; }
    public void setJioSignBaseUrl(String url) { this.jioSignBaseUrl = url; }


    public RequestSpecification getRequestSpecification() { return requestSpecification; }
    public void setRequestSpecification(RequestSpecification requestSpecification) { this.requestSpecification = requestSpecification; }


    public void put(String key, Object value) { extras.put(key, value); }
    public Object get(String key) { return extras.get(key); }
    public boolean contains(String key) { return extras.containsKey(key); }
}
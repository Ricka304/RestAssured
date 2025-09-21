package config;

import context.TestContext;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * ConfigManager
 *
 * - Loads base and environment-specific properties from classpath resources (config.properties, config-<env>.properties).
 * - Honors overrides in this precedence:
 *      1) System property (-Dkey=value)
 *      2) Environment variable (UPPERCASE underscore)
 *      3) env-specific properties (config-<env>.properties)
 *      4) base config (config.properties)
 * - For mock service base URLs, if the per-thread TestContext provides a URL (set at runtime by MockServerConfig),
 *   that value is returned first. This keeps runtime per-thread state out of this class (single responsibility)
 *   while letting tests automatically pick up dynamic mock ports.
 */
public final class ConfigManager {

    private static final Properties baseProps = new Properties();
    private static final Properties envProps = new Properties();
    private static final String BASE_CONFIG = "config.properties";

    static {
        loadBaseConfigProperties();
        String env = getEnvironment(); // reads from system property or baseProps
        if (env == null || env.trim().isEmpty()) {
            System.err.println("Warning: 'environment' not set in " + BASE_CONFIG + ". Defaulting to 'local'");
            env = "local";
        }
        loadEnvironmentSpecificProperties(env);
    }

    private ConfigManager() { /* utility class */ }

    private static void loadBaseConfigProperties() {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(BASE_CONFIG)) {
            if (in != null) {
                baseProps.load(in);
            } else {
                System.err.println("Warning: " + BASE_CONFIG + " not found on classpath");
            }
        } catch (IOException e) {
            System.err.println("Failed to load " + BASE_CONFIG);
            e.printStackTrace();
        }
    }

    private static void loadEnvironmentSpecificProperties(String environment) {
        String fileName = "config-" + environment.toLowerCase() + ".properties";
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(fileName)) {
            if (in != null) {
                envProps.load(in);
            } else {
                System.err.println("Warning: " + fileName + " not found on classpath");
            }
        } catch (IOException e) {
            System.err.println("Failed to load " + fileName);
            e.printStackTrace();
        }
    }

    /** Public helper to reload configuration (useful in tests) */
    public static synchronized void reload() {
        baseProps.clear();
        envProps.clear();
        loadBaseConfigProperties();
        loadEnvironmentSpecificProperties(getEnvironment());
    }

    // -------- Generic getters with system property / env var overrides --------
    private static String getWithOverrides(String propKey, String envKey, String defaultValue) {
        // 1. System property
        String val = System.getProperty(propKey);
        if (val != null && !val.isEmpty()) return val;

        // 2. Environment variable (uppercase underscore convention)
        val = System.getenv(envKey);
        if (val != null && !val.isEmpty()) return val;

        // 3. env-specific props
        val = envProps.getProperty(propKey);
        if (val != null && !val.isEmpty()) return val;

        // 4. base props
        val = baseProps.getProperty(propKey);
        if (val != null && !val.isEmpty()) return val;

        return defaultValue;
    }

    // -------- Core getters --------

    public static String getEnvironment() {
        // allow overriding by -Denvironment=prod
        String env = System.getProperty("environment");
        if (env != null && !env.isEmpty()) return env;
        env = baseProps.getProperty("environment");
        if (env != null && !env.isEmpty()) return env;
        // fallback
        return "local";
    }

    public static String getBaseUri() {
        return getWithOverrides("baseUri", "BASE_URI", null);
    }

    public static String getUserId() {
        return getWithOverrides("userId", "USER_ID", null);
    }

    public static String getPlatformID() {
        return getWithOverrides("platformID", "PLATFORM_ID", null);
    }

    public static String getCompanyID() {
        return getWithOverrides("companyID", "COMPANY_ID", null);
    }

    public static String getAuthToken() {
        return getWithOverrides("authToken", "AUTH_TOKEN", null);
    }

    // -------- Mock service getters (support runtime overrides like dynamic ports) --------
    // Order of precedence:
    // 0) Per-thread TestContext (if present and a URL was set at runtime)
    // 1) system property "mock.<service>.baseUri" (or "mock.<service>.port")
    // 2) environment variable (MOCK_<SERVICE>_BASEURI or MOCK_<SERVICE>_PORT)
    // 3) config.properties or config-<env>.properties entry
    // Example usage: -Dmock.digilocker.port=12345

    public static String getDigilockerBaseUrl() {
        // 0) Try TestContext (per-thread runtime value), if available
        try {
            TestContext ctx = TestContext.get();
            if (ctx != null) {
                String threadVal = ctx.getDigilockerBaseUrl();
                if (threadVal != null && !threadVal.isEmpty()) return threadVal;
            }
        } catch (Throwable ignored) {
            // If TestContext not on classpath or throws, ignore and continue to config fallbacks.
        }

        // 1) Full baseUri override
        String override = getWithOverrides("digilockerMockService.baseUri", "MOCK_DIGILOCKER_BASEURI", null);
        if (override != null) return override;

        // 2) Port override
        String portOverride = getWithOverrides("mock.digilocker.port", "MOCK_DIGILOCKER_PORT", null);
        if (portOverride != null) {
            return "http://localhost:" + portOverride;
        }

        // 3) Fallback to properties
        String configured = envProps.getProperty("digilockerMockService.baseUri");
        if (configured != null && !configured.isEmpty()) return configured;
        configured = baseProps.getProperty("digilockerMockService.baseUri");
        if (configured != null && !configured.isEmpty()) return configured;

        return null;
    }

    public static String getPaymentMockBaseUrl() {
        // 0) TestContext
        try {
            TestContext ctx = TestContext.get();
            if (ctx != null) {
                String threadVal = ctx.getPaymentBaseUrl();
                if (threadVal != null && !threadVal.isEmpty()) return threadVal;
            }
        } catch (Throwable ignored) {}

        String override = getWithOverrides("paymentMockService.baseUri", "MOCK_PAYMENT_BASEURI", null);
        if (override != null) return override;

        String portOverride = getWithOverrides("mock.payment.port", "MOCK_PAYMENT_PORT", null);
        if (portOverride != null) return "http://localhost:" + portOverride;

        String configured = envProps.getProperty("paymentMockService.baseUri");
        if (configured != null && !configured.isEmpty()) return configured;
        configured = baseProps.getProperty("paymentMockService.baseUri");
        if (configured != null && !configured.isEmpty()) return configured;

        return null;
    }

    public static String getJioSignMockBaseUrl() {
        // 0) TestContext
        try {
            TestContext ctx = TestContext.get();
            if (ctx != null) {
                String threadVal = ctx.getJioSignBaseUrl();
                if (threadVal != null && !threadVal.isEmpty()) return threadVal;
            }
        } catch (Throwable ignored) {}

        String override = getWithOverrides("jioSignMockService.baseUri", "MOCK_JIOSIGN_BASEURI", null);
        if (override != null) return override;

        String portOverride = getWithOverrides("mock.jiosign.port", "MOCK_JIOSIGN_PORT", null);
        if (portOverride != null) return "http://localhost:" + portOverride;

        String configured = envProps.getProperty("jioSignMockService.baseUri");
        if (configured != null && !configured.isEmpty()) return configured;
        configured = baseProps.getProperty("jioSignMockService.baseUri");
        if (configured != null && !configured.isEmpty()) return configured;

        return null;
    }

    // utility: check a property exists (helpful for debugging)
    public static boolean hasProperty(String key) {
        return baseProps.containsKey(key) || envProps.containsKey(key) ||
                System.getProperty(key) != null || System.getenv(key.toUpperCase()) != null;
    }
}

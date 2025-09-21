package base;

import config.ConfigManager;
import config.MockServerConfig;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.specification.RequestSpecification;
import mocks.DigilockerServiceMock;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import utils.RequestResponseLogger;
import context.TestContext;

import java.lang.reflect.Method;

public class BaseTest {
    public String userID;
    public String platformID;
    public String companyID;
    public String environment;

    @BeforeClass
    public void globalSetUp() {
        // Do NOT call ExtentReportsManager.createInstance() here if you're using ExtentTestNGListener
        setUpEnvironment();

    }

    public void setUpEnvironment() {
        RestAssured.baseURI = ConfigManager.getBaseUri();
        userID = ConfigManager.getUserId();
        platformID = ConfigManager.getPlatformID();
        companyID = ConfigManager.getCompanyID();
        environment = ConfigManager.getEnvironment();

        // Add request/response logging filter (ensure RequestResponseLogger uses ExtentTestManager.getTest())
        RestAssured.filters(new RequestResponseLogger());
    }

    // Remove ExtentTestManager.startTest(...) — listener will start tests for you
    @BeforeMethod
    public void beforeMethod(Method method) {
        // keep only non-Extent logging/metadata here
        // e.g., log to console or add TestNG attributes if needed
        System.out.println("Starting test: " + this.getClass().getSimpleName() + "." + method.getName());
        MockServerConfig.startMockServersForThread();


// optionally set expectations on this thread's mock server(s)
        DigilockerServiceMock.setupExpectation(MockServerConfig.getDigilockerServerForThread());


// create a RequestSpecification pre-configured for the test to use (example uses digilocker as default)
        String digBase = TestContext.get().getDigilockerBaseUrl();
        RequestSpecification rs = new RequestSpecBuilder()
                .setBaseUri(digBase)
                .setContentType("application/json")
                .build();
        TestContext.get().setRequestSpecification(rs);
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        // You no longer need to manually set Extent pass/fail here.
        // Let the listener (onTestSuccess / onTestFailure / onTestSkipped) mark the ExtentTest status.
        // But you can still do per-test cleanup here.

        // Example: print outcome for debugging
        System.out.println("Test " + result.getMethod().getMethodName() + " finished with status: " + result.getStatus());
        // stop per-thread mock servers
        MockServerConfig.stopMockServersForThread();



// clear per-thread request spec and remove context to avoid leaks
        TestContext.get().setRequestSpecification(null);
        TestContext.remove();
    }

    @AfterClass
    public void afterClass() {

        // DO NOT call ExtentReportsManager.flushReports() here if listener handles it in onFinish(ISuite).
    }
}

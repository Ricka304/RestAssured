package base;

import config.ConfigManager;
import config.MockServerConfig;
import io.restassured.RestAssured;
import mocks.DigilockerServiceMock;
import org.testng.ITestResult;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import utils.ExtentReportsManager;
import utils.ExtentTestManager;
import utils.RequestResponseLogger;

import java.lang.reflect.Method;



public class BaseTest {
    public String userID;
    public String platformID;
    public String companyID;
    public String environment;


        @BeforeClass
        public void globalSetUp() {
            // Initialize ExtentReports
            ExtentReportsManager.createInstance();
            
            setUpEnvironment();
            MockServerConfig.startMockServers();
            DigilockerServiceMock.setupExpectation(MockServerConfig.digilockerServiceMock);

        }

        public void setUpEnvironment()
    {
        RestAssured.baseURI = ConfigManager.getBaseUri();
        userID= ConfigManager.getUserId();
        platformID=ConfigManager.getPlatformID();
        companyID=ConfigManager.getCompanyID();
        environment=ConfigManager.getEnvironment();
        
        // Add request/response logging filter
        RestAssured.filters(new RequestResponseLogger());

    }



    @BeforeMethod
    public void beforeMethod(Method method) {
        // Start ExtentTest for each test method
        String testName = method.getName();
        String className = this.getClass().getSimpleName();
        ExtentTestManager.startTest(className + " - " + testName);
        ExtentTestManager.logInfo("Starting test: " + testName);
        ExtentTestManager.addCategory("API Tests");
        ExtentTestManager.addAuthor("Ricka Sanjeev");
    }

    @AfterMethod
    public void afterMethod(ITestResult result) {
        // Automatically log test result based on TestNG status
        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                ExtentTestManager.logPass("✅ Test completed successfully");
                break;
            case ITestResult.FAILURE:
                ExtentTestManager.logFail("❌ Test failed: " + result.getThrowable().getMessage());
                break;
            case ITestResult.SKIP:
                ExtentTestManager.logSkip("⏭️ Test was skipped: " + result.getThrowable().getMessage());
                break;
        }
        
        // End the current test (clean up thread mapping)
        ExtentTestManager.endTest();
    }

    @AfterClass
    public void stopMockServer() {
        MockServerConfig.stopMockServers();
        // Flush ExtentReports
        ExtentReportsManager.flushReports();
    }
}

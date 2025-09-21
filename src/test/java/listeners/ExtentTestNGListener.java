package listeners;

import org.testng.*;
import utils.ExtentReportsManager;
import utils.ExtentTestManager;


public class ExtentTestNGListener implements ITestListener, ISuiteListener {


    @Override
    public void onStart(ISuite suite) {
// initialize Extent once per suite
        ExtentReportsManager.createInstance();
    }


    @Override
    public void onFinish(ISuite suite) {
        ExtentReportsManager.flushReports();
    }


    @Override
    public void onTestStart(ITestResult result) {
        String testName = result.getTestClass().getName() + "." + result.getMethod().getMethodName();
        ExtentTestManager.startTest(testName);
        ExtentTestManager.logInfo("Test started: " + testName);
    }


    @Override
    public void onTestSuccess(ITestResult result) {
        ExtentTestManager.logPass("Test passed");
        ExtentTestManager.endTest();
    }


    @Override
    public void onTestFailure(ITestResult result) {
        Throwable t = result.getThrowable();
        ExtentTestManager.logFail(t);
        ExtentTestManager.endTest();
    }


    @Override
    public void onTestSkipped(ITestResult result) {
        ExtentTestManager.logSkip("Test skipped");
        ExtentTestManager.endTest();
    }


    // unused
    @Override public void onTestFailedButWithinSuccessPercentage(ITestResult result) {}
    @Override public void onStart(ITestContext context) {}
    @Override public void onFinish(ITestContext context) {}
}
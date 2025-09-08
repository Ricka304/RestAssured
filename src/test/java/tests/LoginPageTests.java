package tests;

import base.BaseTest;
import org.testng.annotations.Test;
import requests.LoginPage;
import utils.ExtentTestManager;

public class LoginPageTests extends BaseTest {
    
    @Test
    public void validateLogin()
    {
        try {
            ExtentTestManager.logInfo("Initiating login validation test");
            
            LoginPage loginPage = new LoginPage();
            loginPage.loginToApplicationAndSetSessionIDInCookie();
            
            ExtentTestManager.logPass("Login validation completed successfully");
            
        } catch (Exception e) {
            ExtentTestManager.logFail("Login validation failed: " + e.getMessage());
            throw e;
        }
    }
}

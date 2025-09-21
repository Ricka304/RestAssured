package tests.functional;

import base.BaseTest;
import org.testng.annotations.Test;
import requests.LoginPage;
import utils.ExtentTestManager;

public class LoginPageTests extends BaseTest {
    
    @Test
    public void validateLogin()
    {

            
            LoginPage loginPage = new LoginPage();
            loginPage.loginToApplicationAndSetSessionIDInCookie();
            
            ExtentTestManager.logPass("Login validation completed successfully");
            

    }
}

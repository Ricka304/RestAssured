package tests;

import base.BaseTest;
import config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import requests.HomePage;
import utils.ExtentTestManager;



public class HomePageTests extends BaseTest {

  @Test
    public void validateFetchGSSCallFromHomePage(){
        try {
            ExtentTestManager.logInfo("Starting GSS schemes fetch validation");
            
            HomePage request = new HomePage();
            Response res= request.fetchGssSchemes();
            
            ExtentTestManager.logInfo("Response received: " + res.getStatusCode());
            res.prettyPrint();
            
            Assert.assertEquals(res.getStatusCode(),200);
            ExtentTestManager.logPass("GSS schemes fetch validation passed - Status code: 200");
            
        } catch (AssertionError | Exception e) {
            ExtentTestManager.logFail("GSS schemes fetch validation failed: " + e.getMessage());
            throw e;
        }
    }
@Test
public void validateFetchGSSCall2FromHomePage() {
    try {
        ExtentTestManager.logInfo("Testing Digilocker mock service endpoint");
        
        String mockBaseUrl = ConfigManager.getDigilockerBaserUrl();
        ExtentTestManager.logInfo("Mock Base URL: " + mockBaseUrl);
        
        Response response = RestAssured.given()
                .baseUri(mockBaseUrl)
                .when().get("/user/details");
        
        response.getBody().prettyPrint();
        
        ExtentTestManager.logInfo("Mock service response status: " + response.getStatusCode());
        ExtentTestManager.logPass("Digilocker mock service test completed successfully");
        
    } catch (Exception e) {
        ExtentTestManager.logFail("Digilocker mock service test failed: " + e.getMessage());
        throw e;
    }
}

   /* @Test
    public void validateFetchGSSCall3FromHomePage() {
        HomePage request = new HomePage();
        request.validatefetchSchemesContract();


    }*/

}

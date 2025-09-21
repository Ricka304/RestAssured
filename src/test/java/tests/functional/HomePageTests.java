package tests.functional;

import base.BaseTest;
import config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.Assert;
import org.testng.annotations.Test;
import requests.HomePage;
import utils.ExtentTestManager;



public class HomePageTests extends BaseTest {

  @Test(dependsOnGroups = "homepage-contract")
    public void validateFetchGSSCallFromHomePage(){

            
            HomePage request = new HomePage();
            Response res= request.fetchGssSchemes();
            
            ExtentTestManager.logInfo("Response received: " + res.getStatusCode());
            res.prettyPrint();
            
            Assert.assertEquals(res.getStatusCode(),200);
            ExtentTestManager.logPass("GSS schemes fetch validation passed - Status code: 200");
            

    }
@Test
public void validateFetchGSSCall2FromHomePage() {

        
        String mockBaseUrl = ConfigManager.getDigilockerBaseUrl();
        ExtentTestManager.logInfo("Mock Base URL: " + mockBaseUrl);
        
        Response response = RestAssured.given()
                .baseUri(mockBaseUrl)
                .when().get("/user/details");
        
        response.getBody().prettyPrint();
        
        ExtentTestManager.logInfo("Mock service response status: " + response.getStatusCode());
        ExtentTestManager.logPass("Digilocker mock service test completed successfully");
        

}

   /* @Test
    public void validateFetchGSSCall3FromHomePage() {
        HomePage request = new HomePage();
        request.();

esign-->X-true-esignDone,DocumentId-dummyID
Payment->jioonepay-transactionid-succeess,51662377213
REceipt download-51662377213,succeess
    }*/

}

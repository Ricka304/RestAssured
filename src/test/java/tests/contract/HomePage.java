package tests.contract;
import config.ConfigManager;
import config.Constants;
import endpoints.GSSEndpoints;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import utils.ExtentTestManager;

import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class HomePage {


    @Test(groups = {"homepage-contract", "contracts"})
    public void validatefetchSchemesContract() {


            Response response =
                    given()
                            .pathParam("param1", GSSEndpoints.EXTENSION_PATH_PARAM)
                            .pathParam("param2", GSSEndpoints.FETCH_SCHEMES)
                            .header("Content-Type", Constants.CONTENT_TYPE_JSON)
                            .header("UserId", ConfigManager.getUserId())
                            .when()
                            .get("{param1}{param2}");

            ExtentTestManager.logInfo("Response received: " + response.getStatusCode());

            // ✅ Hard assertion on status code
            org.testng.Assert.assertEquals(response.getStatusCode(), 200,
                    "Expected status code 200 but got " + response.getStatusCode());

            // ✅ Schema validation assertion
            response.then().assertThat()
                    .body(matchesJsonSchemaInClasspath("jsonSchemas/fetchSchemes.json"));

            ExtentTestManager.logPass("fetchSchemes Contract test passed - Status code: 200");


    }
}
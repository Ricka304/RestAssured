package requests;
import config.ConfigManager;
import config.Constants;
import endpoints.GSSEndpoints;
import io.restassured.RestAssured;
import io.restassured.response.Response;

import static io.restassured.RestAssured.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;

public class HomePage  {


    public Response fetchGssSchemes() {
        RestAssured.useRelaxedHTTPSValidation();
        System.out.println("Calling URL: " + RestAssured.baseURI + GSSEndpoints.FETCH_SCHEMES);
        System.out.println("Environment: " + ConfigManager.getEnvironment());
        System.out.println("User Id : " + ConfigManager.getUserId());
        System.out.println("Platform ID: " +ConfigManager.getPlatformID() );
        System.out.println("Company ID : " + ConfigManager.getCompanyID());


        return  given()
                .pathParam("param1",GSSEndpoints.EXTENSION_PATH_PARAM)
                .pathParam("param2",GSSEndpoints.FETCH_SCHEMES)
                .header("Content-Type", Constants.CONTENT_TYPE_JSON)
                .header("UserId",ConfigManager.getUserId())
                .when().get("{param1}{param2}");

    }


}

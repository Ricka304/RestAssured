package requests.GSSdashboard;


import config.ConfigManager;
import config.Constants;
import endpoints.GSSEndpoints;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import static io.restassured.RestAssured.given;

public class dashboardLoginPage {


    public class LoginPage {
        private String requestID;
        public void loginToApplicationAndSetSessionIDInCookie()
        {
            loginToApplication();
            FetchSessionIDAndSetCookie(requestID);
        }

        public String loginToApplication(){
            RestAssured.useRelaxedHTTPSValidation();
            System.out.println("Calling URL: " + RestAssured.baseURI + GSSEndpoints.PLATFORM_PATH_PARAM+"/user/authentication/v1.0/login/otp?platform="+ ConfigManager.getPlatformID());
            String requestBody = "{ \"mobile\": \"9160037765\", \"country_code\": \"91\" }";
            Response res = RestAssured.given()
                    .pathParam("param1", GSSEndpoints.PLATFORM_PATH_PARAM)
                    .pathParam("param2",ConfigManager.getPlatformID())
                    .header("Authorization","Bearer s%3AlSXV2FnQDLy-l7TCZcBDW6aZx3EM0yBl.zreBxeOjpTRnLZJNFLxNp7UXy6XoQgAYDkTgxXGlbKA")
                    .header("Content-Type", Constants.CONTENT_TYPE_JSON)
                    .when()
                    .body("{\"mobile\":\"9160037765\",\"country_code\":\"91\"}")
                    .post("{param1}/user/authentication/v1.0/login/otp?platform={param2}");
            System.out.println(res.getBody().asString());
            requestID = res.jsonPath().getString("request_id");
            return  requestID;





        }
        public void FetchSessionIDAndSetCookie(String request_id){
            String body= "{\"request_id\":\""+request_id+"\",\"otp\":\"5401\"}";
            System.out.println("Calling URL: " + RestAssured.baseURI + GSSEndpoints.PLATFORM_PATH_PARAM+"/user/authentication/v1.0/login/otp?platform="+ConfigManager.getPlatformID());

            String cookie=  RestAssured.given()
                    .pathParam("param1",GSSEndpoints.PLATFORM_PATH_PARAM)
                    .pathParam("param2",ConfigManager.getPlatformID())
                    .header("Authorization","Bearer NjYzMzNmYzIwYzY4MmEyN2VkYWZjYjVkOl9VczhxRmVNbg==")
                    .header("Content-Type", Constants.CONTENT_TYPE_JSON)
                    .cookie("x.test",true)
                    .when()
                    .body(body)
                    .post("{param1}/user/authentication/v1.0/otp/mobile/verify?platform={param2}").getCookie("f.session");

            RestAssured.requestSpecification = new RequestSpecBuilder()
                    .addCookie("f.session", "s%3AlSXV2FnQDLy-l7TCZcBDW6aZx3EM0yBl.zreBxeOjpTRnLZJNFLxNp7UXy6XoQgAYDkTgxXGlbKA")
                    .build();
        }

    }

}

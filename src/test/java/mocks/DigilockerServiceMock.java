package mocks;
import org.mockserver.client.MockServerClient;
import static org.mockserver.model.HttpRequest.request;
import static org.mockserver.model.HttpResponse.response;

public class DigilockerServiceMock {
       public static void setupExpectation(MockServerClient client) {
            client.when(
                    request()
                            .withMethod("GET")
                            .withPath("/user/details")
            ).respond(
                    response()
                            .withStatusCode(200)
                            .withBody("{ \"name\": \"Ricka\", \"role\": \"QA\" }")
            );
        }
    }






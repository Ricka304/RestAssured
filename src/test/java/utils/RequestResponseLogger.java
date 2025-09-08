package utils;

import io.restassured.filter.Filter;
import io.restassured.filter.FilterContext;
import io.restassured.response.Response;
import io.restassured.specification.FilterableRequestSpecification;
import io.restassured.specification.FilterableResponseSpecification;

import java.util.HashMap;
import java.util.Map;

public class RequestResponseLogger implements Filter {

    @Override
    public Response filter(FilterableRequestSpecification requestSpec, 
                          FilterableResponseSpecification responseSpec, 
                          FilterContext ctx) {
        
        // Log request details
        logRequestDetails(requestSpec);
        
        // Execute the request
        Response response = ctx.next(requestSpec, responseSpec);
        
        // Log response details
        logResponseDetails(response);
        
        return response;
    }

    private void logRequestDetails(FilterableRequestSpecification requestSpec) {
        try {
            String method = requestSpec.getMethod();
            String uri = requestSpec.getURI();
            String body = requestSpec.getBody() != null ? requestSpec.getBody().toString() : "";
            
            // Log headers
            Map<String, String> headers = new HashMap<>();
            if (requestSpec.getHeaders() != null) {
                requestSpec.getHeaders().forEach(header -> 
                    headers.put(header.getName(), header.getValue()));
            }
            
            ExtentTestManager.logRequest(uri, method, body);
            ExtentTestManager.logHeaders(headers);
            
        } catch (Exception e) {
            ExtentTestManager.logWarning("Error logging request details: " + e.getMessage());
        }
    }

    private void logResponseDetails(Response response) {
        try {
            int statusCode = response.getStatusCode();
            String responseBody = response.getBody().asString();
            long responseTime = response.getTime();
            
            ExtentTestManager.logResponse(statusCode, responseBody, responseTime);
            
            // Log response headers if needed
            Map<String, String> responseHeaders = new HashMap<>();
            response.getHeaders().forEach(header -> 
                responseHeaders.put(header.getName(), header.getValue()));
            
            if (!responseHeaders.isEmpty()) {
                ExtentTestManager.getTest().info("<b>Response Headers:</b>");
                responseHeaders.forEach((key, value) -> 
                    ExtentTestManager.getTest().info(key + ": " + value));
            }
            
        } catch (Exception e) {
            ExtentTestManager.logWarning("Error logging response details: " + e.getMessage());
        }
    }
}

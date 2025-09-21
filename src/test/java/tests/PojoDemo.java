package tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import pojo.LoginRequests.*;
import pojo.LoginResponse.*;
import utils.ExtentTestManager;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * PojoDemo - Comprehensive demonstration of POJO usage
 * 
 * This test class demonstrates:
 * 1. Creating request POJOs using different patterns (Builder, Constructor, Setters)
 * 2. Serializing POJOs to JSON for API calls
 * 3. Deserializing JSON responses to POJO objects
 * 4. Validation of input data using Bean Validation
 * 5. Utility methods and business logic
 * 6. Real-world usage patterns in API testing
 */
public class PojoDemo {
    
    private ObjectMapper objectMapper;
    private Validator validator;
    
    @BeforeClass
    public void setUp() {
        objectMapper = new ObjectMapper();
        
        // Initialize Bean Validation
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        
        ExtentTestManager.logInfo("POJO Demo Test Suite - Showcasing comprehensive POJO usage");
    }
    
    @Test(priority = 1)
    public void demonstrateOtpLoginRequestCreation() {
        ExtentTestManager.logInfo("🧪 Testing OtpLoginRequest creation patterns");
        
        try {
            // Pattern 1: Builder Pattern (Recommended)
            OtpLoginRequest builderRequest = OtpLoginRequest.builder()
                    .mobile("9160037765")
                    .countryCode("91")
                    .platformId("GSS_APP")
                    .deviceId("DEVICE_12345")
                    .appVersion("1.0.0")
                    .build();
            
            ExtentTestManager.logInfo("✅ Builder Pattern: " + builderRequest.toString());
            
            // Pattern 2: Constructor
            OtpLoginRequest constructorRequest = new OtpLoginRequest("9876543210", "91");
            constructorRequest.setPlatformId("GSS_WEB");
            
            ExtentTestManager.logInfo("✅ Constructor Pattern: " + constructorRequest.toString());
            
            // Pattern 3: Default constructor + setters
            OtpLoginRequest setterRequest = new OtpLoginRequest();
            setterRequest.setMobile("9123456789");
            setterRequest.setCountryCode("91");
            setterRequest.setDeviceId("WEB_BROWSER");
            
            ExtentTestManager.logInfo("✅ Setter Pattern: " + setterRequest.toString());
            
            // Test utility methods
            String fullNumber = builderRequest.getFullMobileNumber();
            boolean isValidMobile = builderRequest.isValidMobile();
            boolean isValidCountryCode = builderRequest.isValidCountryCode();
            
            ExtentTestManager.logInfo("📱 Full Mobile: " + fullNumber);
            ExtentTestManager.logInfo("✅ Valid Mobile: " + isValidMobile);
            ExtentTestManager.logInfo("✅ Valid Country Code: " + isValidCountryCode);
            
            ExtentTestManager.logPass("OtpLoginRequest creation patterns demonstrated successfully");
            
        } catch (Exception e) {
            ExtentTestManager.logFail("Failed to demonstrate OtpLoginRequest creation: " + e.getMessage());
            throw e;
        }
    }
    
    @Test(priority = 2)
    public void demonstrateJsonSerialization() {
        ExtentTestManager.logInfo("🔄 Testing JSON serialization/deserialization");
        
        try {
            // Create a comprehensive request
            OtpLoginRequest request = OtpLoginRequest.builder()
                    .mobile("9160037765")
                    .countryCode("91")
                    .platformId("GSS_MOBILE")
                    .deviceId("iPhone_14_Pro")
                    .appVersion("2.1.0")
                    .build();
            
            // Serialize to JSON
            String jsonString;
            OtpLoginRequest deserializedRequest;
            try {
                jsonString = objectMapper.writeValueAsString(request);
                ExtentTestManager.logInfo("📤 Serialized JSON: " + jsonString);
                
                // Deserialize back to object
                deserializedRequest = objectMapper.readValue(jsonString, OtpLoginRequest.class);
            } catch (JsonProcessingException e) {
                ExtentTestManager.logFail("JSON serialization/deserialization failed: " + e.getMessage());
                throw new RuntimeException(e);
            }
            ExtentTestManager.logInfo("📥 Deserialized Object: " + deserializedRequest.toString());
            
            // Verify they are equal
            boolean areEqual = request.equals(deserializedRequest);
            ExtentTestManager.logInfo("🔍 Objects Equal: " + areEqual);
            
            if (areEqual) {
                ExtentTestManager.logPass("JSON serialization/deserialization successful");
            } else {
                ExtentTestManager.logFail("Serialization/deserialization failed - objects not equal");
            }
            
        } catch (Exception e) {
            ExtentTestManager.logFail("JSON processing failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
    
    @Test(priority = 3)
    public void demonstrateValidation() {
        ExtentTestManager.logInfo("✅ Testing Bean Validation");
        
        try {
            // Test 1: Valid request - should pass validation
            OtpLoginRequest validRequest = OtpLoginRequest.builder()
                    .mobile("9160037765")
                    .countryCode("91")
                    .build();
            
            Set<ConstraintViolation<OtpLoginRequest>> validViolations = validator.validate(validRequest);
            ExtentTestManager.logInfo("✅ Valid Request Violations: " + validViolations.size());
            
            // Test 2: Invalid mobile - should fail validation
            OtpLoginRequest invalidMobileRequest = OtpLoginRequest.builder()
                    .mobile("123") // Too short
                    .countryCode("91")
                    .build();
            
            Set<ConstraintViolation<OtpLoginRequest>> mobileViolations = validator.validate(invalidMobileRequest);
            ExtentTestManager.logInfo("❌ Invalid Mobile Violations: " + mobileViolations.size());
            for (ConstraintViolation<OtpLoginRequest> violation : mobileViolations) {
                ExtentTestManager.logInfo("  - " + violation.getPropertyPath() + ": " + violation.getMessage());
            }
            
            // Test 3: Invalid country code - should fail validation
            OtpLoginRequest invalidCountryRequest = OtpLoginRequest.builder()
                    .mobile("9160037765")
                    .countryCode("") // Empty
                    .build();
            
            Set<ConstraintViolation<OtpLoginRequest>> countryViolations = validator.validate(invalidCountryRequest);
            ExtentTestManager.logInfo("❌ Invalid Country Code Violations: " + countryViolations.size());
            for (ConstraintViolation<OtpLoginRequest> violation : countryViolations) {
                ExtentTestManager.logInfo("  - " + violation.getPropertyPath() + ": " + violation.getMessage());
            }
            
            // Test OTP Verification Request Validation
            OtpVerificationRequest validOtpRequest = OtpVerificationRequest.builder()
                    .requestId("550e8400-e29b-41d4-a716-446655440000")
                    .otp("5401")
                    .build();
            
            Set<ConstraintViolation<OtpVerificationRequest>> otpViolations = validator.validate(validOtpRequest);
            ExtentTestManager.logInfo("✅ Valid OTP Request Violations: " + otpViolations.size());
            
            ExtentTestManager.logPass("Bean Validation demonstration completed successfully");
            
        } catch (Exception e) {
            ExtentTestManager.logFail("Validation demonstration failed: " + e.getMessage());
            throw e;
        }
    }
    
    @Test(priority = 4)
    public void demonstrateResponseDeserialization() {
        ExtentTestManager.logInfo("📥 Testing Response POJO deserialization");
        
        try {
            // Simulate OTP Login Response JSON (from actual API)
            String otpLoginResponseJson = """
                {
                    "success": true,
                    "request_id": "667f7af0-898d-11f0-b1dd-4f346ba50fed",
                    "message": "OTP sent",
                    "mobile": "9160037765",
                    "country_code": "91",
                    "resend_timer": 90,
                    "resend_token": "667f7af0-898d-11f0-b1dd-4f346ba50fed",
                    "max_attempts": 3,
                    "attempts_remaining": 3
                }""";
            
            // Deserialize to POJO
            OtpLoginResponse otpResponse;
            try {
                otpResponse = objectMapper.readValue(otpLoginResponseJson, OtpLoginResponse.class);
            } catch (JsonProcessingException e) {
                ExtentTestManager.logFail("Failed to deserialize OTP login response: " + e.getMessage());
                throw new RuntimeException(e);
            }
            
            ExtentTestManager.logInfo("📱 OTP Response: " + otpResponse.toString());
            ExtentTestManager.logInfo("✅ Success: " + otpResponse.isSuccessful());
            ExtentTestManager.logInfo("🔢 Full Mobile: " + otpResponse.getFullMobileNumber());
            ExtentTestManager.logInfo("🔄 Can Resend: " + otpResponse.canResend());
            ExtentTestManager.logInfo("⚡ Has Attempts: " + otpResponse.hasAttemptsRemaining());
            
            // Simulate OTP Verification Response JSON
            String otpVerificationResponseJson = """
                {
                    "success": true,
                    "message": "Login successful",
                    "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
                    "refresh_token": "refresh_token_xyz123",
                    "token_type": "Bearer",
                    "expires_in": 3600,
                    "session_id": "sess_12345",
                    "user_id": "user_98765",
                    "user_profile": {
                        "user_id": "user_98765",
                        "email": "test@example.com",
                        "mobile": "9160037765",
                        "first_name": "Test",
                        "last_name": "User",
                        "is_verified": true
                    },
                    "permissions": ["read", "write"],
                    "roles": ["user"],
                    "is_new_user": false
                }""";
            
            // Deserialize verification response
            OtpVerificationResponse verificationResponse;
            try {
                verificationResponse = objectMapper.readValue(
                        otpVerificationResponseJson, OtpVerificationResponse.class);
            } catch (JsonProcessingException e) {
                ExtentTestManager.logFail("Failed to deserialize OTP verification response: " + e.getMessage());
                throw new RuntimeException(e);
            }
            
            ExtentTestManager.logInfo("🔐 Verification Response: " + verificationResponse.toString());
            ExtentTestManager.logInfo("✅ Has Valid Tokens: " + verificationResponse.hasValidTokens());
            ExtentTestManager.logInfo("🔑 Bearer Token: " + verificationResponse.getBearerToken());
            ExtentTestManager.logInfo("👤 Display Name: " + verificationResponse.getUserProfile().getDisplayName());
            ExtentTestManager.logInfo("🛡️ Has Read Permission: " + verificationResponse.hasPermission("read"));
            ExtentTestManager.logInfo("👨‍💼 Has User Role: " + verificationResponse.hasRole("user"));
            
            // Test Error Response
            String errorResponseJson = """
                {
                    "success": false,
                    "error_code": "INVALID_OTP",
                    "error_message": "The OTP you entered is invalid",
                    "timestamp": "2024-01-15T10:30:00Z",
                    "validation_errors": [
                        {
                            "field": "otp",
                            "message": "OTP must be 4-6 digits",
                            "rejected_value": "12"
                        }
                    ]
                }""";
            
            ErrorResponse errorResponse;
            try {
                errorResponse = objectMapper.readValue(errorResponseJson, ErrorResponse.class);
            } catch (JsonProcessingException e) {
                ExtentTestManager.logFail("Failed to deserialize error response: " + e.getMessage());
                throw new RuntimeException(e);
            }
            ExtentTestManager.logInfo("❌ Error Response: " + errorResponse.toString());
            ExtentTestManager.logInfo("🔍 Has Validation Errors: " + errorResponse.hasValidationErrors());
            
            ExtentTestManager.logPass("Response deserialization demonstrated successfully");
            
        } catch (Exception e) {
            ExtentTestManager.logFail("Response deserialization failed: " + e.getMessage());
            throw e;
        }
    }
    
    @Test(priority = 5)
    public void demonstrateRealWorldUsage() {
        ExtentTestManager.logInfo("🌍 Demonstrating real-world API testing usage");
        
        try {
            // Scenario: User login flow simulation
            ExtentTestManager.logInfo("🔄 Simulating complete login flow");
            
            // Step 1: Create OTP request
            OtpLoginRequest otpRequest = OtpLoginRequest.builder()
                    .mobile("9160037765")
                    .countryCode("91")
                    .platformId("GSS_WEB")
                    .deviceId("browser_session_123")
                    .appVersion("3.0.0")
                    .build();
            
            // Validate request before sending
            Set<ConstraintViolation<OtpLoginRequest>> violations = validator.validate(otpRequest);
            if (!violations.isEmpty()) {
                ExtentTestManager.logFail("Request validation failed");
                return;
            }
            
            String requestJson;
            try {
                requestJson = objectMapper.writeValueAsString(otpRequest);
                ExtentTestManager.logInfo("📤 Sending OTP Request: " + requestJson);
            } catch (JsonProcessingException e) {
                ExtentTestManager.logFail("Failed to serialize OTP request: " + e.getMessage());
                throw new RuntimeException(e);
            }
            
            // Step 2: Simulate successful OTP response
            Map<String, Object> responseMap = new HashMap<>();
            responseMap.put("success", true);
            responseMap.put("request_id", "req_12345");
            responseMap.put("message", "OTP sent successfully");
            responseMap.put("mobile", otpRequest.getMobile());
            responseMap.put("country_code", otpRequest.getCountryCode());
            responseMap.put("resend_timer", 90);
            responseMap.put("resend_token", "resend_token_xyz");
            
            String simulatedResponseJson;
            OtpLoginResponse otpResponse;
            try {
                simulatedResponseJson = objectMapper.writeValueAsString(responseMap);
                otpResponse = objectMapper.readValue(simulatedResponseJson, OtpLoginResponse.class);
            } catch (JsonProcessingException e) {
                ExtentTestManager.logFail("Failed to process simulated response JSON: " + e.getMessage());
                throw new RuntimeException(e);
            }
            
            // Step 3: Validate response
            if (!otpResponse.isSuccessful()) {
                ExtentTestManager.logFail("OTP request failed: " + otpResponse.getErrorMessage());
                return;
            }
            
            ExtentTestManager.logInfo("✅ OTP sent to: " + otpResponse.getFullMobileNumber());
            
            // Step 4: Create verification request
            OtpVerificationRequest verificationRequest = OtpVerificationRequest.builder()
                    .requestId(otpResponse.getRequestId())
                    .otp("5401")
                    .deviceId("browser_session_123")
                    .rememberDevice(true)
                    .build();
            
            String verificationJson;
            try {
                verificationJson = objectMapper.writeValueAsString(verificationRequest);
                ExtentTestManager.logInfo("📤 Sending Verification Request: " + verificationJson);
            } catch (JsonProcessingException e) {
                ExtentTestManager.logFail("Failed to serialize verification request: " + e.getMessage());
                throw new RuntimeException(e);
            }
            
            // Step 5: Simulate successful verification
            UserProfile userProfile = new UserProfile();
            userProfile.setUserId("user_12345");
            userProfile.setEmail("test.user@example.com");
            userProfile.setMobile(otpRequest.getMobile());
            userProfile.setFirstName("Test");
            userProfile.setLastName("User");
            userProfile.setIsVerified(true);
            
            OtpVerificationResponse verificationResponse = new OtpVerificationResponse();
            verificationResponse.setSuccess(true);
            verificationResponse.setMessage("Login successful");
            verificationResponse.setAccessToken("jwt_token_abc123");
            verificationResponse.setRefreshToken("refresh_token_xyz789");
            verificationResponse.setTokenType("Bearer");
            verificationResponse.setExpiresIn(3600L);
            verificationResponse.setSessionId("session_98765");
            verificationResponse.setUserId("user_12345");
            verificationResponse.setUserProfile(userProfile);
            verificationResponse.setPermissions(Arrays.asList("read", "write", "admin"));
            verificationResponse.setRoles(Arrays.asList("user", "premium"));
            verificationResponse.setIsNewUser(false);
            
            // Step 6: Validate final response
            if (!verificationResponse.hasValidTokens()) {
                ExtentTestManager.logFail("Invalid tokens received");
                return;
            }
            
            ExtentTestManager.logInfo("🎉 Login successful for: " + verificationResponse.getUserProfile().getDisplayName());
            ExtentTestManager.logInfo("🔑 Access Token: " + verificationResponse.getBearerToken().substring(0, 20) + "...");
            ExtentTestManager.logInfo("⏰ Token expires in: " + verificationResponse.getExpiresIn() + " seconds");
            ExtentTestManager.logInfo("🛡️ Permissions: " + verificationResponse.getPermissions());
            ExtentTestManager.logInfo("👨‍💼 Roles: " + verificationResponse.getRoles());
            
            // Demonstrate token refresh simulation
            RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                    .refreshToken(verificationResponse.getRefreshToken())
                    .deviceId("browser_session_123")
                    .appVersion("3.0.0")
                    .build();
            
            ExtentTestManager.logInfo("🔄 Token Refresh Request: " + refreshRequest.toString());
            
            ExtentTestManager.logPass("Real-world login flow simulation completed successfully");
            
        } catch (Exception e) {
            ExtentTestManager.logFail("Real-world usage demonstration failed: " + e.getMessage());
            throw e;
        }
    }
    
    @Test(priority = 6)
    public void demonstrateSecurityFeatures() {
        ExtentTestManager.logInfo("🔒 Demonstrating security features in POJOs");
        
        try {
            // Demonstrate sensitive data masking in toString()
            OtpLoginRequest sensitiveRequest = OtpLoginRequest.builder()
                    .mobile("9876543210")
                    .countryCode("91")
                    .build();
            
            ExtentTestManager.logInfo("🔐 Masked Mobile in toString(): " + sensitiveRequest.toString());
            
            OtpVerificationRequest otpRequest = OtpVerificationRequest.builder()
                    .requestId("req_12345")
                    .otp("5401")
                    .build();
            
            ExtentTestManager.logInfo("🔐 Masked OTP in toString(): " + otpRequest.toString());
            
            // Demonstrate token truncation
            RefreshTokenRequest tokenRequest = RefreshTokenRequest.builder()
                    .refreshToken("very_long_refresh_token_that_should_be_truncated_for_security")
                    .build();
            
            ExtentTestManager.logInfo("🔐 Truncated Token in toString(): " + tokenRequest.toString());
            
            // Demonstrate user profile masking
            UserProfile profile = new UserProfile();
            profile.setMobile("9876543210");
            profile.setEmail("sensitive@example.com");
            profile.setFirstName("John");
            profile.setLastName("Doe");
            
            ExtentTestManager.logInfo("🔐 Masked Profile: " + profile.toString());
            
            ExtentTestManager.logPass("Security features demonstrated successfully");
            
        } catch (Exception e) {
            ExtentTestManager.logFail("Security features demonstration failed: " + e.getMessage());
            throw e;
        }
    }
}

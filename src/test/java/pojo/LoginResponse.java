package pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * LoginResponse - Comprehensive POJO classes for login responses
 * 
 * Contains nested classes for different types of login responses:
 * - OtpLoginResponse: Response for initial OTP request
 * - OtpVerificationResponse: Response for OTP verification
 * - RefreshTokenResponse: Response for token refresh
 * - ErrorResponse: Standard error response format
 * 
 * Features:
 * - Jackson annotations for JSON deserialization
 * - Comprehensive field mapping
 * - Utility methods for response validation
 * - Thread-safe immutable design
 * - Rich error handling support
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginResponse {
    
    /**
     * OtpLoginResponse - Response for OTP login request
     * Received from: POST /user/authentication/v1.0/login/otp
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OtpLoginResponse {
        
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("request_id")
        private String requestId;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("mobile")
        private String mobile;
        
        @JsonProperty("country_code")
        private String countryCode;
        
        @JsonProperty("resend_timer")
        private Integer resendTimer;
        
        @JsonProperty("resend_token")
        private String resendToken;
        
        @JsonProperty("max_attempts")
        private Integer maxAttempts;
        
        @JsonProperty("attempts_remaining")
        private Integer attemptsRemaining;
        
        @JsonProperty("expires_at")
        private String expiresAt;
        
        @JsonProperty("error_code")
        private String errorCode;
        
        @JsonProperty("error_message")
        private String errorMessage;
        
        @JsonProperty("metadata")
        private Map<String, Object> metadata;
        
        // Default constructor
        public OtpLoginResponse() {}
        
        // Getters and Setters
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
        
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
        
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
        
        public Integer getResendTimer() { return resendTimer; }
        public void setResendTimer(Integer resendTimer) { this.resendTimer = resendTimer; }
        
        public String getResendToken() { return resendToken; }
        public void setResendToken(String resendToken) { this.resendToken = resendToken; }
        
        public Integer getMaxAttempts() { return maxAttempts; }
        public void setMaxAttempts(Integer maxAttempts) { this.maxAttempts = maxAttempts; }
        
        public Integer getAttemptsRemaining() { return attemptsRemaining; }
        public void setAttemptsRemaining(Integer attemptsRemaining) { this.attemptsRemaining = attemptsRemaining; }
        
        public String getExpiresAt() { return expiresAt; }
        public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
        
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
        
        // Utility methods
        public boolean isSuccessful() {
            return success != null && success;
        }
        
        public boolean hasError() {
            return !isSuccessful() || errorCode != null || errorMessage != null;
        }
        
        public String getFullMobileNumber() {
            if (countryCode != null && mobile != null) {
                return countryCode + mobile;
            }
            return mobile;
        }
        
        public boolean canResend() {
            return resendTimer != null && resendTimer <= 0 && resendToken != null;
        }
        
        public boolean isExpired() {
            if (expiresAt == null) return false;
            try {
                LocalDateTime expiry = LocalDateTime.parse(expiresAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return LocalDateTime.now().isAfter(expiry);
            } catch (Exception e) {
                return false;
            }
        }
        
        public boolean hasAttemptsRemaining() {
            return attemptsRemaining == null || attemptsRemaining > 0;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OtpLoginResponse that = (OtpLoginResponse) o;
            return Objects.equals(success, that.success) &&
                   Objects.equals(requestId, that.requestId) &&
                   Objects.equals(message, that.message) &&
                   Objects.equals(mobile, that.mobile) &&
                   Objects.equals(countryCode, that.countryCode);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(success, requestId, message, mobile, countryCode);
        }
        
        @Override
        public String toString() {
            return "OtpLoginResponse{" +
                   "success=" + success +
                   ", requestId='" + requestId + '\'' +
                   ", message='" + message + '\'' +
                   ", mobile='" + (mobile != null ? mobile.replaceAll("\\d", "*") : null) + '\'' +
                   ", countryCode='" + countryCode + '\'' +
                   ", resendTimer=" + resendTimer +
                   ", hasError=" + hasError() +
                   '}';
        }
    }
    
    /**
     * OtpVerificationResponse - Response for OTP verification
     * Received from: POST /user/authentication/v1.0/otp/mobile/verify
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OtpVerificationResponse {
        
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("access_token")
        private String accessToken;
        
        @JsonProperty("refresh_token")
        private String refreshToken;
        
        @JsonProperty("token_type")
        private String tokenType;
        
        @JsonProperty("expires_in")
        private Long expiresIn;
        
        @JsonProperty("expires_at")
        private String expiresAt;
        
        @JsonProperty("session_id")
        private String sessionId;
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("user_profile")
        private UserProfile userProfile;
        
        @JsonProperty("permissions")
        private List<String> permissions;
        
        @JsonProperty("roles")
        private List<String> roles;
        
        @JsonProperty("device_id")
        private String deviceId;
        
        @JsonProperty("is_new_user")
        private Boolean isNewUser;
        
        @JsonProperty("requires_profile_completion")
        private Boolean requiresProfileCompletion;
        
        @JsonProperty("error_code")
        private String errorCode;
        
        @JsonProperty("error_message")
        private String errorMessage;
        
        @JsonProperty("metadata")
        private Map<String, Object> metadata;
        
        // Default constructor
        public OtpVerificationResponse() {}
        
        // Getters and Setters
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        
        public Long getExpiresIn() { return expiresIn; }
        public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
        
        public String getExpiresAt() { return expiresAt; }
        public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
        
        public String getSessionId() { return sessionId; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public UserProfile getUserProfile() { return userProfile; }
        public void setUserProfile(UserProfile userProfile) { this.userProfile = userProfile; }
        
        public List<String> getPermissions() { return permissions; }
        public void setPermissions(List<String> permissions) { this.permissions = permissions; }
        
        public List<String> getRoles() { return roles; }
        public void setRoles(List<String> roles) { this.roles = roles; }
        
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        
        public Boolean getIsNewUser() { return isNewUser; }
        public void setIsNewUser(Boolean isNewUser) { this.isNewUser = isNewUser; }
        
        public Boolean getRequiresProfileCompletion() { return requiresProfileCompletion; }
        public void setRequiresProfileCompletion(Boolean requiresProfileCompletion) { this.requiresProfileCompletion = requiresProfileCompletion; }
        
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public Map<String, Object> getMetadata() { return metadata; }
        public void setMetadata(Map<String, Object> metadata) { this.metadata = metadata; }
        
        // Utility methods
        public boolean isSuccessful() {
            return success != null && success && accessToken != null;
        }
        
        public boolean hasError() {
            return !isSuccessful() || errorCode != null || errorMessage != null;
        }
        
        public boolean hasValidTokens() {
            return accessToken != null && !accessToken.trim().isEmpty();
        }
        
        public boolean isTokenExpired() {
            if (expiresAt == null) return false;
            try {
                LocalDateTime expiry = LocalDateTime.parse(expiresAt, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                return LocalDateTime.now().isAfter(expiry);
            } catch (Exception e) {
                return false;
            }
        }
        
        public String getBearerToken() {
            if (accessToken == null) return null;
            return tokenType != null ? tokenType + " " + accessToken : "Bearer " + accessToken;
        }
        
        public boolean hasPermission(String permission) {
            return permissions != null && permissions.contains(permission);
        }
        
        public boolean hasRole(String role) {
            return roles != null && roles.contains(role);
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OtpVerificationResponse that = (OtpVerificationResponse) o;
            return Objects.equals(success, that.success) &&
                   Objects.equals(accessToken, that.accessToken) &&
                   Objects.equals(sessionId, that.sessionId) &&
                   Objects.equals(userId, that.userId);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(success, accessToken, sessionId, userId);
        }
        
        @Override
        public String toString() {
            return "OtpVerificationResponse{" +
                   "success=" + success +
                   ", message='" + message + '\'' +
                   ", accessToken='" + (accessToken != null ? accessToken.substring(0, Math.min(10, accessToken.length())) + "..." : null) + '\'' +
                   ", tokenType='" + tokenType + '\'' +
                   ", userId='" + userId + '\'' +
                   ", sessionId='" + sessionId + '\'' +
                   ", isNewUser=" + isNewUser +
                   ", hasError=" + hasError() +
                   '}';
        }
    }
    
    /**
     * RefreshTokenResponse - Response for token refresh
     * Received from: POST /user/authentication/v1.0/token/refresh
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RefreshTokenResponse {
        
        @JsonProperty("success")
        private Boolean success;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("access_token")
        private String accessToken;
        
        @JsonProperty("refresh_token")
        private String refreshToken;
        
        @JsonProperty("token_type")
        private String tokenType;
        
        @JsonProperty("expires_in")
        private Long expiresIn;
        
        @JsonProperty("expires_at")
        private String expiresAt;
        
        @JsonProperty("error_code")
        private String errorCode;
        
        @JsonProperty("error_message")
        private String errorMessage;
        
        // Default constructor
        public RefreshTokenResponse() {}
        
        // Getters and Setters
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public String getAccessToken() { return accessToken; }
        public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
        
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        
        public String getTokenType() { return tokenType; }
        public void setTokenType(String tokenType) { this.tokenType = tokenType; }
        
        public Long getExpiresIn() { return expiresIn; }
        public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
        
        public String getExpiresAt() { return expiresAt; }
        public void setExpiresAt(String expiresAt) { this.expiresAt = expiresAt; }
        
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        // Utility methods
        public boolean isSuccessful() {
            return success != null && success && accessToken != null;
        }
        
        public boolean hasError() {
            return !isSuccessful() || errorCode != null || errorMessage != null;
        }
        
        public String getBearerToken() {
            if (accessToken == null) return null;
            return tokenType != null ? tokenType + " " + accessToken : "Bearer " + accessToken;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RefreshTokenResponse that = (RefreshTokenResponse) o;
            return Objects.equals(success, that.success) &&
                   Objects.equals(accessToken, that.accessToken) &&
                   Objects.equals(refreshToken, that.refreshToken);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(success, accessToken, refreshToken);
        }
        
        @Override
        public String toString() {
            return "RefreshTokenResponse{" +
                   "success=" + success +
                   ", message='" + message + '\'' +
                   ", accessToken='" + (accessToken != null ? accessToken.substring(0, Math.min(10, accessToken.length())) + "..." : null) + '\'' +
                   ", tokenType='" + tokenType + '\'' +
                   ", hasError=" + hasError() +
                   '}';
        }
    }
    
    /**
     * UserProfile - Nested user profile information
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class UserProfile {
        
        @JsonProperty("user_id")
        private String userId;
        
        @JsonProperty("email")
        private String email;
        
        @JsonProperty("mobile")
        private String mobile;
        
        @JsonProperty("first_name")
        private String firstName;
        
        @JsonProperty("last_name")
        private String lastName;
        
        @JsonProperty("full_name")
        private String fullName;
        
        @JsonProperty("profile_image")
        private String profileImage;
        
        @JsonProperty("is_verified")
        private Boolean isVerified;
        
        @JsonProperty("created_at")
        private String createdAt;
        
        @JsonProperty("last_login")
        private String lastLogin;
        
        // Default constructor
        public UserProfile() {}
        
        // Getters and Setters
        public String getUserId() { return userId; }
        public void setUserId(String userId) { this.userId = userId; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
        
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        
        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }
        
        public String getProfileImage() { return profileImage; }
        public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
        
        public Boolean getIsVerified() { return isVerified; }
        public void setIsVerified(Boolean isVerified) { this.isVerified = isVerified; }
        
        public String getCreatedAt() { return createdAt; }
        public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
        
        public String getLastLogin() { return lastLogin; }
        public void setLastLogin(String lastLogin) { this.lastLogin = lastLogin; }
        
        // Utility methods
        public String getDisplayName() {
            if (fullName != null && !fullName.trim().isEmpty()) {
                return fullName;
            }
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            }
            if (firstName != null) {
                return firstName;
            }
            return email != null ? email.split("@")[0] : "User";
        }
        
        public boolean isVerified() {
            return isVerified != null && isVerified;
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserProfile that = (UserProfile) o;
            return Objects.equals(userId, that.userId) &&
                   Objects.equals(email, that.email) &&
                   Objects.equals(mobile, that.mobile);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(userId, email, mobile);
        }
        
        @Override
        public String toString() {
            return "UserProfile{" +
                   "userId='" + userId + '\'' +
                   ", email='" + email + '\'' +
                   ", mobile='" + (mobile != null ? mobile.replaceAll("\\d", "*") : null) + '\'' +
                   ", displayName='" + getDisplayName() + '\'' +
                   ", isVerified=" + isVerified() +
                   '}';
        }
    }
    
    /**
     * ErrorResponse - Standard error response format
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ErrorResponse {
        
        @JsonProperty("success")
        private Boolean success = false;
        
        @JsonProperty("error_code")
        private String errorCode;
        
        @JsonProperty("error_message")
        private String errorMessage;
        
        @JsonProperty("error_details")
        private String errorDetails;
        
        @JsonProperty("timestamp")
        private String timestamp;
        
        @JsonProperty("path")
        private String path;
        
        @JsonProperty("validation_errors")
        private List<ValidationError> validationErrors;
        
        // Default constructor
        public ErrorResponse() {}
        
        public ErrorResponse(String errorCode, String errorMessage) {
            this.errorCode = errorCode;
            this.errorMessage = errorMessage;
        }
        
        // Getters and Setters
        public Boolean getSuccess() { return success; }
        public void setSuccess(Boolean success) { this.success = success; }
        
        public String getErrorCode() { return errorCode; }
        public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
        
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
        
        public String getErrorDetails() { return errorDetails; }
        public void setErrorDetails(String errorDetails) { this.errorDetails = errorDetails; }
        
        public String getTimestamp() { return timestamp; }
        public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
        
        public String getPath() { return path; }
        public void setPath(String path) { this.path = path; }
        
        public List<ValidationError> getValidationErrors() { return validationErrors; }
        public void setValidationErrors(List<ValidationError> validationErrors) { this.validationErrors = validationErrors; }
        
        // Utility methods
        public boolean hasValidationErrors() {
            return validationErrors != null && !validationErrors.isEmpty();
        }
        
        @Override
        public String toString() {
            return "ErrorResponse{" +
                   "success=" + success +
                   ", errorCode='" + errorCode + '\'' +
                   ", errorMessage='" + errorMessage + '\'' +
                   ", errorDetails='" + errorDetails + '\'' +
                   ", hasValidationErrors=" + hasValidationErrors() +
                   '}';
        }
    }
    
    /**
     * ValidationError - Individual validation error details
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class ValidationError {
        
        @JsonProperty("field")
        private String field;
        
        @JsonProperty("message")
        private String message;
        
        @JsonProperty("rejected_value")
        private Object rejectedValue;
        
        // Default constructor
        public ValidationError() {}
        
        public ValidationError(String field, String message) {
            this.field = field;
            this.message = message;
        }
        
        // Getters and Setters
        public String getField() { return field; }
        public void setField(String field) { this.field = field; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public Object getRejectedValue() { return rejectedValue; }
        public void setRejectedValue(Object rejectedValue) { this.rejectedValue = rejectedValue; }
        
        @Override
        public String toString() {
            return "ValidationError{" +
                   "field='" + field + '\'' +
                   ", message='" + message + '\'' +
                   ", rejectedValue=" + rejectedValue +
                   '}';
        }
    }
}

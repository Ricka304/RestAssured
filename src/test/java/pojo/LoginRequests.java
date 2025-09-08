package pojo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.util.Objects;

/**
 * LoginRequests - Comprehensive POJO classes for login operations
 * 
 * Contains nested classes for different types of login requests:
 * - OtpLoginRequest: Initial OTP request with mobile number
 * - OtpVerificationRequest: OTP verification with request_id and OTP
 * - RefreshTokenRequest: Token refresh request
 * 
 * Features:
 * - Jackson annotations for JSON serialization
 * - Validation annotations for input validation
 * - Builder pattern for easy object creation
 * - Proper equals, hashCode, toString implementations
 * - Thread-safe design
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LoginRequests {
    
    /**
     * OtpLoginRequest - Request for initiating OTP login
     * Used for: POST /user/authentication/v1.0/login/otp
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OtpLoginRequest {
        
        @JsonProperty("mobile")
        @NotBlank(message = "Mobile number is required")
        @Pattern(regexp = "^[0-9]{10}$", message = "Mobile number must be 10 digits")
        private String mobile;
        
        @JsonProperty("country_code")
        @NotBlank(message = "Country code is required")
        @Pattern(regexp = "^[0-9]{1,4}$", message = "Country code must be 1-4 digits")
        private String countryCode;
        
        @JsonProperty("platform_id")
        private String platformId;
        
        @JsonProperty("device_id")
        private String deviceId;
        
        @JsonProperty("app_version")
        private String appVersion;
        
        // Default constructor for Jackson
        public OtpLoginRequest() {}
        
        // Full constructor
        public OtpLoginRequest(String mobile, String countryCode) {
            this.mobile = mobile;
            this.countryCode = countryCode;
        }
        
        // Full constructor with optional fields
        public OtpLoginRequest(String mobile, String countryCode, String platformId, String deviceId, String appVersion) {
            this.mobile = mobile;
            this.countryCode = countryCode;
            this.platformId = platformId;
            this.deviceId = deviceId;
            this.appVersion = appVersion;
        }
        
        // Builder pattern
        public static class Builder {
            private String mobile;
            private String countryCode;
            private String platformId;
            private String deviceId;
            private String appVersion;
            
            public Builder mobile(String mobile) {
                this.mobile = mobile;
                return this;
            }
            
            public Builder countryCode(String countryCode) {
                this.countryCode = countryCode;
                return this;
            }
            
            public Builder platformId(String platformId) {
                this.platformId = platformId;
                return this;
            }
            
            public Builder deviceId(String deviceId) {
                this.deviceId = deviceId;
                return this;
            }
            
            public Builder appVersion(String appVersion) {
                this.appVersion = appVersion;
                return this;
            }
            
            public OtpLoginRequest build() {
                return new OtpLoginRequest(mobile, countryCode, platformId, deviceId, appVersion);
            }
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        // Getters and Setters
        public String getMobile() { return mobile; }
        public void setMobile(String mobile) { this.mobile = mobile; }
        
        public String getCountryCode() { return countryCode; }
        public void setCountryCode(String countryCode) { this.countryCode = countryCode; }
        
        public String getPlatformId() { return platformId; }
        public void setPlatformId(String platformId) { this.platformId = platformId; }
        
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        
        public String getAppVersion() { return appVersion; }
        public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
        
        // Utility methods
        public String getFullMobileNumber() {
            return countryCode + mobile;
        }
        
        public boolean isValidMobile() {
            return mobile != null && mobile.matches("^[0-9]{10}$");
        }
        
        public boolean isValidCountryCode() {
            return countryCode != null && countryCode.matches("^[0-9]{1,4}$");
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OtpLoginRequest that = (OtpLoginRequest) o;
            return Objects.equals(mobile, that.mobile) &&
                   Objects.equals(countryCode, that.countryCode) &&
                   Objects.equals(platformId, that.platformId) &&
                   Objects.equals(deviceId, that.deviceId) &&
                   Objects.equals(appVersion, that.appVersion);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(mobile, countryCode, platformId, deviceId, appVersion);
        }
        
        @Override
        public String toString() {
            return "OtpLoginRequest{" +
                   "mobile='" + (mobile != null ? mobile.replaceAll("\\d", "*") : null) + '\'' +
                   ", countryCode='" + countryCode + '\'' +
                   ", platformId='" + platformId + '\'' +
                   ", deviceId='" + deviceId + '\'' +
                   ", appVersion='" + appVersion + '\'' +
                   '}';
        }
    }
    
    /**
     * OtpVerificationRequest - Request for verifying OTP
     * Used for: POST /user/authentication/v1.0/otp/mobile/verify
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class OtpVerificationRequest {
        
        @JsonProperty("request_id")
        @NotBlank(message = "Request ID is required")
        private String requestId;
        
        @JsonProperty("otp")
        @NotBlank(message = "OTP is required")
        @Size(min = 4, max = 6, message = "OTP must be 4-6 digits")
        @Pattern(regexp = "^[0-9]{4,6}$", message = "OTP must contain only digits")
        private String otp;
        
        @JsonProperty("device_id")
        private String deviceId;
        
        @JsonProperty("remember_device")
        private Boolean rememberDevice;
        
        // Default constructor
        public OtpVerificationRequest() {}
        
        // Essential constructor
        public OtpVerificationRequest(String requestId, String otp) {
            this.requestId = requestId;
            this.otp = otp;
        }
        
        // Full constructor
        public OtpVerificationRequest(String requestId, String otp, String deviceId, Boolean rememberDevice) {
            this.requestId = requestId;
            this.otp = otp;
            this.deviceId = deviceId;
            this.rememberDevice = rememberDevice;
        }
        
        // Builder pattern
        public static class Builder {
            private String requestId;
            private String otp;
            private String deviceId;
            private Boolean rememberDevice;
            
            public Builder requestId(String requestId) {
                this.requestId = requestId;
                return this;
            }
            
            public Builder otp(String otp) {
                this.otp = otp;
                return this;
            }
            
            public Builder deviceId(String deviceId) {
                this.deviceId = deviceId;
                return this;
            }
            
            public Builder rememberDevice(Boolean rememberDevice) {
                this.rememberDevice = rememberDevice;
                return this;
            }
            
            public OtpVerificationRequest build() {
                return new OtpVerificationRequest(requestId, otp, deviceId, rememberDevice);
            }
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        // Getters and Setters
        public String getRequestId() { return requestId; }
        public void setRequestId(String requestId) { this.requestId = requestId; }
        
        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
        
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        
        public Boolean getRememberDevice() { return rememberDevice; }
        public void setRememberDevice(Boolean rememberDevice) { this.rememberDevice = rememberDevice; }
        
        // Utility methods
        public boolean isValidOtp() {
            return otp != null && otp.matches("^[0-9]{4,6}$");
        }
        
        public boolean isValidRequestId() {
            return requestId != null && !requestId.trim().isEmpty();
        }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            OtpVerificationRequest that = (OtpVerificationRequest) o;
            return Objects.equals(requestId, that.requestId) &&
                   Objects.equals(otp, that.otp) &&
                   Objects.equals(deviceId, that.deviceId) &&
                   Objects.equals(rememberDevice, that.rememberDevice);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(requestId, otp, deviceId, rememberDevice);
        }
        
        @Override
        public String toString() {
            return "OtpVerificationRequest{" +
                   "requestId='" + requestId + '\'' +
                   ", otp='" + (otp != null ? "*".repeat(otp.length()) : null) + '\'' +
                   ", deviceId='" + deviceId + '\'' +
                   ", rememberDevice=" + rememberDevice +
                   '}';
        }
    }
    
    /**
     * RefreshTokenRequest - Request for refreshing authentication token
     * Used for: POST /user/authentication/v1.0/token/refresh
     */
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class RefreshTokenRequest {
        
        @JsonProperty("refresh_token")
        @NotBlank(message = "Refresh token is required")
        private String refreshToken;
        
        @JsonProperty("device_id")
        private String deviceId;
        
        @JsonProperty("app_version")
        private String appVersion;
        
        // Default constructor
        public RefreshTokenRequest() {}
        
        // Constructor
        public RefreshTokenRequest(String refreshToken) {
            this.refreshToken = refreshToken;
        }
        
        public RefreshTokenRequest(String refreshToken, String deviceId, String appVersion) {
            this.refreshToken = refreshToken;
            this.deviceId = deviceId;
            this.appVersion = appVersion;
        }
        
        // Builder pattern
        public static class Builder {
            private String refreshToken;
            private String deviceId;
            private String appVersion;
            
            public Builder refreshToken(String refreshToken) {
                this.refreshToken = refreshToken;
                return this;
            }
            
            public Builder deviceId(String deviceId) {
                this.deviceId = deviceId;
                return this;
            }
            
            public Builder appVersion(String appVersion) {
                this.appVersion = appVersion;
                return this;
            }
            
            public RefreshTokenRequest build() {
                return new RefreshTokenRequest(refreshToken, deviceId, appVersion);
            }
        }
        
        public static Builder builder() {
            return new Builder();
        }
        
        // Getters and Setters
        public String getRefreshToken() { return refreshToken; }
        public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
        
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        
        public String getAppVersion() { return appVersion; }
        public void setAppVersion(String appVersion) { this.appVersion = appVersion; }
        
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RefreshTokenRequest that = (RefreshTokenRequest) o;
            return Objects.equals(refreshToken, that.refreshToken) &&
                   Objects.equals(deviceId, that.deviceId) &&
                   Objects.equals(appVersion, that.appVersion);
        }
        
        @Override
        public int hashCode() {
            return Objects.hash(refreshToken, deviceId, appVersion);
        }
        
        @Override
        public String toString() {
            return "RefreshTokenRequest{" +
                   "refreshToken='" + (refreshToken != null ? refreshToken.substring(0, Math.min(10, refreshToken.length())) + "..." : null) + '\'' +
                   ", deviceId='" + deviceId + '\'' +
                   ", appVersion='" + appVersion + '\'' +
                   '}';
        }
    }
}

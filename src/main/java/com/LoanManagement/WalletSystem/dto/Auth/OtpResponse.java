package com.LoanManagement.WalletSystem.dto.Auth;

/**
 * DTO for OTP operations response
 */
public class OtpResponse {
    private String message;
    private String email;
    private Long expiresIn; // in seconds

    public OtpResponse() {}

    public OtpResponse(String message, String email, Long expiresIn) {
        this.message = message;
        this.email = email;
        this.expiresIn = expiresIn;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getExpiresIn() {
        return expiresIn;
    }

    public void setExpiresIn(Long expiresIn) {
        this.expiresIn = expiresIn;
    }
}


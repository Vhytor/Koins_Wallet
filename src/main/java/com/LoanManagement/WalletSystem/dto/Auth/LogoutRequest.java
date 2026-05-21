package com.LoanManagement.WalletSystem.dto.Auth;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for logout request
 */
public class LogoutRequest {
    @NotBlank(message = "Token cannot be blank")
    private String token;

    public LogoutRequest() {}

    public LogoutRequest(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}


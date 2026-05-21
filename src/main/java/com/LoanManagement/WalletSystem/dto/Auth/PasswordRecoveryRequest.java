package com.LoanManagement.WalletSystem.dto.Auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * DTO for password recovery (forgot password) request
 */
public class PasswordRecoveryRequest {
    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    public PasswordRecoveryRequest() {}

    public PasswordRecoveryRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}


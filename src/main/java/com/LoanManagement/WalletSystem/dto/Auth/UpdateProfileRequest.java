package com.LoanManagement.WalletSystem.dto.Auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * DTO for updating user profile
 */
public class UpdateProfileRequest {
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100, message = "Full name must be between 2 and 100 characters")
    private String fullName;

    @Pattern(regexp = "^[+]?[0-9]{10,15}$", message = "Phone number must be valid")
    private String phone;

    @Size(max = 11, message = "BVN must be 11 digits")
    private String bvn;

    public UpdateProfileRequest() {}

    public UpdateProfileRequest(String fullName, String phone, String bvn) {
        this.fullName = fullName;
        this.phone = phone;
        this.bvn = bvn;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getBvn() {
        return bvn;
    }

    public void setBvn(String bvn) {
        this.bvn = bvn;
    }
}


package com.LoanManagement.WalletSystem.dto.Auth;

import java.time.Instant;

public class UserResponse {
    private String id;
    private String fullName;
    private String email;
    private String phone;
    private Integer accountStatus;
    private Instant createdAt;

    public UserResponse() {}

    public UserResponse(String id, String fullName, String email, String phone, Integer accountStatus, Instant createdAt) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.accountStatus = accountStatus;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Integer getAccountStatus() { return accountStatus; }
    public void setAccountStatus(Integer accountStatus) { this.accountStatus = accountStatus; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}


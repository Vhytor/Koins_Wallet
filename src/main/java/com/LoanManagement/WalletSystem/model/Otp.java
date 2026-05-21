package com.LoanManagement.WalletSystem.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * OTP entity for managing one-time passwords for password recovery
 * Follows JPA best practices with proper lifecycle management
 */
@Entity
@Table(name = "otp", indexes = {
    @Index(name = "idx_otp_email", columnList = "email"),
    @Index(name = "idx_otp_expires_at", columnList = "expires_at")
})
public class Otp {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "attempts", nullable = false)
    private Integer attempts = 0; // Track verification attempts

    @Column(name = "is_used", nullable = false)
    private Boolean isUsed = false;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public Otp() {}

    public Otp(String email, String code, Instant expiresAt) {
        this.email = email;
        this.code = code;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getAttempts() {
        return attempts;
    }

    public void setAttempts(Integer attempts) {
        this.attempts = attempts;
    }

    public Boolean getIsUsed() {
        return isUsed;
    }

    public void setIsUsed(Boolean isUsed) {
        this.isUsed = isUsed;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Check if OTP is expired
     */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    /**
     * Check if OTP is valid (not expired, not used, not exceeded max attempts)
     */
    public boolean isValid() {
        return !isExpired() && !isUsed && attempts < 5;
    }
}


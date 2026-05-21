package com.LoanManagement.WalletSystem.model;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

/**
 * TokenBlacklist entity for managing invalidated JWT tokens
 * Prevents use of tokens after logout
 * Follows TTL pattern - entries auto-cleanup after tokens expire
 */
@Entity
@Table(name = "token_blacklist", indexes = {
    @Index(name = "idx_token_blacklist_token", columnList = "token"),
    @Index(name = "idx_token_blacklist_expires_at", columnList = "expires_at")
})
public class TokenBlacklist {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @Column(name = "token", nullable = false, unique = true, length = 512)
    private String token;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "blacklisted_at", nullable = false)
    private Instant blacklistedAt = Instant.now();

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt; // When the original token would expire

    public TokenBlacklist() {}

    public TokenBlacklist(String token, String userEmail, Instant expiresAt) {
        this.token = token;
        this.userEmail = userEmail;
        this.expiresAt = expiresAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Instant getBlacklistedAt() {
        return blacklistedAt;
    }

    public void setBlacklistedAt(Instant blacklistedAt) {
        this.blacklistedAt = blacklistedAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * Check if token is still blacklisted (not yet expired)
     */
    public boolean isStillBlacklisted() {
        return Instant.now().isBefore(expiresAt);
    }
}


package com.LoanManagement.WalletSystem.service;

/**
 * Service interface for token blacklist operations
 * Manages logout and token invalidation
 */
public interface TokenBlacklistService {
    /**
     * Blacklist a token (logout)
     * @param token The JWT token to blacklist
     * @param userEmail User's email address
     * @param expiresAt Token expiration time
     */
    void blacklistToken(String token, String userEmail, long expiresAtSeconds);

    /**
     * Check if token is blacklisted
     * @param token The JWT token to check
     * @return true if blacklisted, false otherwise
     */
    boolean isTokenBlacklisted(String token);

    /**
     * Cleanup expired tokens from blacklist
     */
    void cleanupExpiredTokens();
}


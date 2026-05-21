package com.LoanManagement.WalletSystem.service;

import com.LoanManagement.WalletSystem.model.Otp;

/**
 * Service interface for OTP operations
 * Handles OTP generation, validation, and cleanup
 */
public interface OtpService {
    /**
     * Generate and save OTP for password recovery
     * @param email User's email address
     * @return Generated OTP code
     */
    String generateOtp(String email);

    /**
     * Verify if OTP is valid and matches
     * @param email User's email
     * @param code OTP code to verify
     * @return true if valid, false otherwise
     */
    boolean verifyOtp(String email, String code);

    /**
     * Mark OTP as used after successful verification
     * @param email User's email
     * @param code OTP code
     */
    void markOtpAsUsed(String email, String code);

    /**
     * Cleanup expired OTPs
     */
    void cleanupExpiredOtps();

    /**
     * Get remaining time for OTP in seconds
     * @param email User's email
     * @return Seconds remaining, or -1 if OTP doesn't exist
     */
    Long getRemainingOtpTime(String email);
}


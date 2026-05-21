package com.LoanManagement.WalletSystem.service;

/**
 * Service interface for email operations
 * Abstracts email sending functionality
 */
public interface EmailService {
    /**
     * Send OTP to user's email
     * @param email Recipient's email address
     * @param otp The OTP code to send
     */
    void sendOtpEmail(String email, String otp);

    /**
     * Send password recovery confirmation email
     * @param email Recipient's email address
     */
    void sendPasswordResetConfirmationEmail(String email);

    /**
     * Send generic email
     * @param email Recipient's email
     * @param subject Email subject
     * @param body Email body (HTML)
     */
    void sendEmail(String email, String subject, String body);
}


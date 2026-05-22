package com.LoanManagement.WalletSystem.service.impl;

import com.LoanManagement.WalletSystem.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * In production, replace with actual email service (SendGrid, AWS SES, etc.)
 * I'm using a logging approach here for demonstration purposes. The codes are stored in the db
 * cause this is a demo, but in production, you would never log OTPs or sensitive info.
 */
@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Override
    public void sendOtpEmail(String email, String otp) {
        // In production: integrate with actual email service
        logger.info("Sending OTP email to: {} with OTP: {}", email, otp);
        // Email content would be:
        // Subject: Your OTP for Password Reset
        // Body: Your One-Time Password is: {otp}. This code expires in 10 minutes.
    }

    @Override
    public void sendPasswordResetConfirmationEmail(String email) {
        // In production: integrate with actual email service
        logger.info("Sending password reset confirmation email to: {}", email);
        // Email content would be:
        // Subject: Password Reset Successful
        // Body: Your password has been successfully reset. You can now login with your new password.
    }

    @Override
    public void sendEmail(String email, String subject, String body) {
        // In production: integrate with actual email service
        logger.info("Sending email to: {} with subject: {}", email, subject);
        // Implementation would use JavaMailSender or email service provider SDK
    }
}


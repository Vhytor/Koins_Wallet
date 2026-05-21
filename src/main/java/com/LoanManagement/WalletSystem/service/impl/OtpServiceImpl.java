package com.LoanManagement.WalletSystem.service.impl;

import com.LoanManagement.WalletSystem.exception.BusinessRuleException;
import com.LoanManagement.WalletSystem.exception.ResourceNotFoundException;
import com.LoanManagement.WalletSystem.model.Otp;
import com.LoanManagement.WalletSystem.repository.OtpRepository;
import com.LoanManagement.WalletSystem.service.OtpService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Random;

/**
 * Implementation of OtpService
 * Handles OTP generation, validation, and lifecycle management
 */
@Service
public class OtpServiceImpl implements OtpService {

    private final OtpRepository otpRepository;

    @Value("${app.otp.expiration-minutes:10}")
    private int otpExpirationMinutes;

    @Value("${app.otp.max-requests-per-hour:5}")
    private int maxOtpRequestsPerHour;

    private final Random random = new Random();

    public OtpServiceImpl(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    @Override
    @Transactional
    public String generateOtp(String email) {
        // Check rate limiting - max 5 requests per hour
        Instant oneHourAgo = Instant.now().minusSeconds(3600);
        int recentRequests = otpRepository.countRecentOtpRequests(email, oneHourAgo);

        if (recentRequests >= maxOtpRequestsPerHour) {
            throw new BusinessRuleException(
                    "Too many OTP requests. Please try again after 1 hour."
            );
        }

        // Generate 6-digit OTP
        String otpCode = String.format("%06d", random.nextInt(1000000));

        // Set expiration time
        Instant expiresAt = Instant.now().plusSeconds(otpExpirationMinutes * 60L);

        // Create and save OTP
        Otp otp = new Otp(email, otpCode, expiresAt);
        otpRepository.save(otp);

        return otpCode;
    }

    @Override
    public boolean verifyOtp(String email, String code) {
        Otp otp = otpRepository.findByEmailAndCode(email, code)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid OTP"));

        // Check if OTP is valid
        if (!otp.isValid()) {
            if (otp.isExpired()) {
                throw new BusinessRuleException("OTP has expired");
            }
            if (otp.getIsUsed()) {
                throw new BusinessRuleException("OTP has already been used");
            }
            throw new BusinessRuleException("OTP verification attempts exceeded");
        }

        // Increment attempts
        otp.setAttempts(otp.getAttempts() + 1);
        otpRepository.save(otp);

        return true;
    }

    @Override
    @Transactional
    public void markOtpAsUsed(String email, String code) {
        Otp otp = otpRepository.findByEmailAndCode(email, code)
                .orElseThrow(() -> new ResourceNotFoundException("OTP not found"));

        otp.setIsUsed(true);
        otpRepository.save(otp);
    }

    @Override
    @Transactional
    public void cleanupExpiredOtps() {
        otpRepository.deleteByExpiresAtBefore(Instant.now());
    }

    @Override
    public Long getRemainingOtpTime(String email) {
        Otp otp = otpRepository.findTopByEmailAndIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(
                email,
                Instant.now()
        ).orElse(null);

        if (otp == null) {
            return -1L;
        }

        long secondsRemaining = otp.getExpiresAt().getEpochSecond() - Instant.now().getEpochSecond();
        return Math.max(0, secondsRemaining);
    }
}


package com.LoanManagement.WalletSystem.repository;

import com.LoanManagement.WalletSystem.model.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository interface for OTP entity
 * Provides database operations for OTP management
 */
@Repository
public interface OtpRepository extends JpaRepository<Otp, String> {
    /**
     * Find the most recent valid OTP for a given email
     */
    Optional<Otp> findTopByEmailAndIsUsedFalseAndExpiresAtAfterOrderByCreatedAtDesc(String email, Instant now);

    /**
     * Find OTP by email and code
     */
    Optional<Otp> findByEmailAndCode(String email, String code);

    /**
     * Delete expired OTPs (cleanup)
     */
    void deleteByExpiresAtBefore(Instant now);

    /**
     * Count valid OTP attempts for a user within a time window
     */
    @Query("SELECT COUNT(o) FROM Otp o WHERE o.email = :email AND o.createdAt > :since AND o.isUsed = false")
    int countRecentOtpRequests(String email, Instant since);
}


package com.LoanManagement.WalletSystem.repository;

import com.LoanManagement.WalletSystem.model.TokenBlacklist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

/**
 * Repository interface for TokenBlacklist entity
 * Provides database operations for token blacklist management
 */
@Repository
public interface TokenBlacklistRepository extends JpaRepository<TokenBlacklist, String> {
    /**
     * Check if token is blacklisted
     */
    Optional<TokenBlacklist> findByToken(String token);

    /**
     * Cleanup expired tokens (can be called periodically)
     */
    void deleteByExpiresAtBefore(Instant now);

    /**
     * Count blacklisted tokens for a user
     */
    long countByUserEmail(String userEmail);
}


package com.LoanManagement.WalletSystem.service.impl;

import com.LoanManagement.WalletSystem.model.TokenBlacklist;
import com.LoanManagement.WalletSystem.repository.TokenBlacklistRepository;
import com.LoanManagement.WalletSystem.service.TokenBlacklistService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * Implementation of TokenBlacklistService
 * Manages logout and token invalidation using database persistence
 * In high-volume scenarios, consider using Redis for better performance
 */
@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

    private final TokenBlacklistRepository tokenBlacklistRepository;

    public TokenBlacklistServiceImpl(TokenBlacklistRepository tokenBlacklistRepository) {
        this.tokenBlacklistRepository = tokenBlacklistRepository;
    }

    @Override
    @Transactional
    public void blacklistToken(String token, String userEmail, long expiresAtSeconds) {
        Instant expiresAt = Instant.ofEpochSecond(expiresAtSeconds);
        TokenBlacklist blacklistedToken = new TokenBlacklist(token, userEmail, expiresAt);
        tokenBlacklistRepository.save(blacklistedToken);
    }

    @Override
    public boolean isTokenBlacklisted(String token) {
        return tokenBlacklistRepository.findByToken(token)
                .map(TokenBlacklist::isStillBlacklisted)
                .orElse(false);
    }

    @Override
    @Transactional
    public void cleanupExpiredTokens() {
        tokenBlacklistRepository.deleteByExpiresAtBefore(Instant.now());
    }
}


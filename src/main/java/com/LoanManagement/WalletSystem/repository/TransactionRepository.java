package com.LoanManagement.WalletSystem.repository;

import com.LoanManagement.WalletSystem.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Transaction entity
 * Provides database operations for transaction management
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, String> {
    /**
     * Find all transactions for a specific wallet, ordered by creation date (descending)
     */
    List<Transaction> findByWalletIdOrderByCreatedAtDesc(String walletId);

    /**
     * Find all transactions for a specific user, ordered by creation date (descending)
     */
    List<Transaction> findByUserIdOrderByCreatedAtDesc(String userId);

    /**
     * Find a transaction by ID and verify it belongs to the specified user
     */
    Optional<Transaction> findByIdAndUserId(String id, String userId);
}


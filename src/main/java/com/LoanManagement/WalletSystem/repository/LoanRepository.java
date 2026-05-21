package com.LoanManagement.WalletSystem.repository;

import com.LoanManagement.WalletSystem.model.Loan;
import com.LoanManagement.WalletSystem.model.LoanStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Loan entity
 * Follows Repository pattern and provides database access operations
 */
@Repository
public interface LoanRepository extends JpaRepository<Loan, String> {
    
    /**
     * Find all loans for a specific user
     */
    List<Loan> findByUserId(String userId);
    
    /**
     * Find all loans by status
     * Used for admin to manage loans by their status
     */
    List<Loan> findByStatus(LoanStatus status);
    
    /**
     * Find all loans for a user by status
     */
    List<Loan> findByUserIdAndStatus(String userId, LoanStatus status);
    
    /**
     * Find a specific loan by ID and user ID
     * Used for authorization checks
     */
    Optional<Loan> findByIdAndUserId(String id, String userId);
    
    /**
     * Count pending loans for a user
     * Can be used for eligibility checks
     */
    long countByUserIdAndStatus(String userId, LoanStatus status);
}


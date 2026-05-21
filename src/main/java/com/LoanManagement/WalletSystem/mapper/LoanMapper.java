package com.LoanManagement.WalletSystem.mapper;

import com.LoanManagement.WalletSystem.dto.Loan.LoanResponse;
import com.LoanManagement.WalletSystem.model.Loan;

import java.util.List;

/**
 * Mapper interface for Loan entity
 * Follows Mapper pattern for separation of concerns
 */
public interface LoanMapper {
    
    /**
     * Convert Loan entity to LoanResponse DTO
     */
    LoanResponse toLoanResponse(Loan loan);
    
    /**
     * Convert list of Loan entities to list of LoanResponse DTOs
     */
    List<LoanResponse> toLoanResponseList(List<Loan> loans);
}


package com.LoanManagement.WalletSystem.mapper.impl;

import com.LoanManagement.WalletSystem.dto.Loan.LoanResponse;
import com.LoanManagement.WalletSystem.mapper.LoanMapper;
import com.LoanManagement.WalletSystem.model.Loan;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of LoanMapper
 * Handles conversion between Loan entity and LoanResponse DTO
 */
@Component
public class LoanMapperImpl implements LoanMapper {

    @Override
    public LoanResponse toLoanResponse(Loan loan) {
        if (loan == null) {
            return null;
        }
        return new LoanResponse(
                loan.getId(),
                loan.getUser() != null ? loan.getUser().getId() : null,
                loan.getUser() != null ? loan.getUser().getEmail() : null,
                loan.getPrincipalAmount(),
                loan.getInterestRate(),
                loan.getTotalAmount(),
                loan.getRepaidAmount(),
                loan.getRemainingAmount(),
                loan.getDurationMonths(),
                loan.getStatus() != null ? loan.getStatus().toString() : null,
                loan.getReason(),
                loan.getRejectionReason(),
                loan.getAppliedAt(),
                loan.getApprovedAt(),
                loan.getDisbursedAt(),
                loan.getCompletedAt()
        );
    }

    @Override
    public List<LoanResponse> toLoanResponseList(List<Loan> loans) {
        if (loans == null) {
            return List.of();
        }
        return loans.stream()
                .map(this::toLoanResponse)
                .collect(Collectors.toList());
    }
}


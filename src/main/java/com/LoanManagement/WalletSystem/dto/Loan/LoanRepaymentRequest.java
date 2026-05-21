package com.LoanManagement.WalletSystem.dto.Loan;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO for repaying a loan
 */
public class LoanRepaymentRequest {

    @NotNull(message = "Repayment amount is required")
    @DecimalMin(value = "1", message = "Repayment amount must be at least 1")
    private BigDecimal repaymentAmount;

    // Constructors
    public LoanRepaymentRequest() {}

    public LoanRepaymentRequest(BigDecimal repaymentAmount) {
        this.repaymentAmount = repaymentAmount;
    }

    // Getters and Setters
    public BigDecimal getRepaymentAmount() {
        return repaymentAmount;
    }

    public void setRepaymentAmount(BigDecimal repaymentAmount) {
        this.repaymentAmount = repaymentAmount;
    }
}


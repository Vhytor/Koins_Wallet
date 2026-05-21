package com.LoanManagement.WalletSystem.dto.Loan;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO for submitting a new loan application
 * Uses Bean Validation annotations following Spring best practices
 */
public class ApplyLoanRequest {

    @NotNull(message = "Principal amount is required")
    @DecimalMin(value = "1000", message = "Principal amount must be at least 1000")
    private BigDecimal principalAmount;

    @NotNull(message = "Interest rate is required")
    @DecimalMin(value = "0.1", message = "Interest rate must be at least 0.1%")
    private BigDecimal interestRate;

    @NotNull(message = "Duration in months is required")
    @Min(value = 1, message = "Duration must be at least 1 month")
    private Integer durationMonths;

    @NotBlank(message = "Reason for loan is required")
    private String reason;

    // Constructors
    public ApplyLoanRequest() {}

    public ApplyLoanRequest(BigDecimal principalAmount, BigDecimal interestRate, 
                            Integer durationMonths, String reason) {
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.durationMonths = durationMonths;
        this.reason = reason;
    }

    // Getters and Setters
    public BigDecimal getPrincipalAmount() {
        return principalAmount;
    }

    public void setPrincipalAmount(BigDecimal principalAmount) {
        this.principalAmount = principalAmount;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}


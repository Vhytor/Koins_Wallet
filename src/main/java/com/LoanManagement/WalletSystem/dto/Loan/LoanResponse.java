package com.LoanManagement.WalletSystem.dto.Loan;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * DTO for loan responses
 * Used for all loan-related API responses
 */
public class LoanResponse {

    private String id;
    private String userId;
    private String userEmail;
    private BigDecimal principalAmount;
    private BigDecimal interestRate;
    private BigDecimal totalAmount;
    private BigDecimal repaidAmount;
    private BigDecimal remainingAmount;
    private Integer durationMonths;
    private String status;
    private String reason;
    private String rejectionReason;
    private Instant appliedAt;
    private Instant approvedAt;
    private Instant disbursedAt;
    private Instant completedAt;

    // Constructors
    public LoanResponse() {}

    public LoanResponse(String id, String userId, String userEmail, BigDecimal principalAmount,
                        BigDecimal interestRate, BigDecimal totalAmount, BigDecimal repaidAmount,
                        BigDecimal remainingAmount, Integer durationMonths, String status,
                        String reason, String rejectionReason, Instant appliedAt,
                        Instant approvedAt, Instant disbursedAt, Instant completedAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.totalAmount = totalAmount;
        this.repaidAmount = repaidAmount;
        this.remainingAmount = remainingAmount;
        this.durationMonths = durationMonths;
        this.status = status;
        this.reason = reason;
        this.rejectionReason = rejectionReason;
        this.appliedAt = appliedAt;
        this.approvedAt = approvedAt;
        this.disbursedAt = disbursedAt;
        this.completedAt = completedAt;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public BigDecimal getRepaidAmount() {
        return repaidAmount;
    }

    public void setRepaidAmount(BigDecimal repaidAmount) {
        this.repaidAmount = repaidAmount;
    }

    public BigDecimal getRemainingAmount() {
        return remainingAmount;
    }

    public void setRemainingAmount(BigDecimal remainingAmount) {
        this.remainingAmount = remainingAmount;
    }

    public Integer getDurationMonths() {
        return durationMonths;
    }

    public void setDurationMonths(Integer durationMonths) {
        this.durationMonths = durationMonths;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public Instant getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(Instant appliedAt) {
        this.appliedAt = appliedAt;
    }

    public Instant getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(Instant approvedAt) {
        this.approvedAt = approvedAt;
    }

    public Instant getDisbursedAt() {
        return disbursedAt;
    }

    public void setDisbursedAt(Instant disbursedAt) {
        this.disbursedAt = disbursedAt;
    }

    public Instant getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(Instant completedAt) {
        this.completedAt = completedAt;
    }
}


package com.LoanManagement.WalletSystem.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Loan entity representing a loan application and its lifecycle
 * Follows the Repository pattern and Clean Architecture principles
 */
@Entity
@Table(name = "loans")
public class Loan {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id = UUID.randomUUID().toString();

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "principal_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal principalAmount;

    @Column(name = "interest_rate", precision = 5, scale = 2, nullable = false)
    private BigDecimal interestRate; // percentage

    @Column(name = "total_amount", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalAmount; // principal + interest

    @Column(name = "repaid_amount", precision = 19, scale = 2)
    private BigDecimal repaidAmount = BigDecimal.ZERO;

    @Column(name = "remaining_amount", precision = 19, scale = 2)
    private BigDecimal remainingAmount;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private LoanStatus status = LoanStatus.PENDING;

    @Column(name = "reason")
    private String reason; // Reason for the loan

    @Column(name = "rejection_reason")
    private String rejectionReason; // Reason for rejection if rejected

    @Column(name = "applied_at", nullable = false)
    private Instant appliedAt = Instant.now();

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "disbursed_at")
    private Instant disbursedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    public Loan() {}

    public Loan(User user, BigDecimal principalAmount, BigDecimal interestRate, 
                Integer durationMonths, String reason) {
        this.user = user;
        this.principalAmount = principalAmount;
        this.interestRate = interestRate;
        this.durationMonths = durationMonths;
        this.reason = reason;
        this.totalAmount = calculateTotalAmount(principalAmount, interestRate);
        this.remainingAmount = this.totalAmount;
    }

    /**
     * Calculate total amount including interest
     */
    private BigDecimal calculateTotalAmount(BigDecimal principal, BigDecimal rate) {
        BigDecimal interest = principal.multiply(rate).divide(BigDecimal.valueOf(100));
        return principal.add(interest);
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public LoanStatus getStatus() {
        return status;
    }

    public void setStatus(LoanStatus status) {
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


package com.LoanManagement.WalletSystem.dto.Loan;

import jakarta.validation.constraints.NotBlank;

/**
 * DTO for approving or rejecting a loan application
 * Admin uses this to make approval decisions
 */
public class LoanApprovalRequest {

    @NotBlank(message = "Approval decision is required (APPROVED or REJECTED)")
    private String decision; // APPROVED or REJECTED

    private String rejectionReason; // Required if decision is REJECTED

    // Constructors
    public LoanApprovalRequest() {}

    public LoanApprovalRequest(String decision, String rejectionReason) {
        this.decision = decision;
        this.rejectionReason = rejectionReason;
    }

    // Getters and Setters
    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
}


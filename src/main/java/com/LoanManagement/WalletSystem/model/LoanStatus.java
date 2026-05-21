package com.LoanManagement.WalletSystem.model;

/**
 * Enum representing possible loan statuses in the system
 */
public enum LoanStatus {
    PENDING,      // Loan application submitted, awaiting approval
    APPROVED,     // Loan has been approved by admin
    REJECTED,     // Loan application has been rejected
    DISBURSED,    // Loan amount has been disbursed to user's wallet
    ACTIVE,       // Loan is active and awaiting repayment
    COMPLETED,    // Loan has been fully repaid
    DEFAULTED     // Loan repayment is in default
}


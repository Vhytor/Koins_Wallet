package com.LoanManagement.WalletSystem.service;

import com.LoanManagement.WalletSystem.dto.Loan.ApplyLoanRequest;
import com.LoanManagement.WalletSystem.dto.Loan.LoanApprovalRequest;
import com.LoanManagement.WalletSystem.dto.Loan.LoanRepaymentRequest;
import com.LoanManagement.WalletSystem.dto.Loan.LoanResponse;

import java.util.List;

/**
 * Service interface for Loan operations
 * Defines contract for loan-related business logic
 * Follows Repository and Facade patterns
 */
public interface LoanService {

    /**
     * Apply for a new loan
     * User submits their loan application with required details
     * 
     * @param userEmail Email of the user applying for loan
     * @param request Loan application request
     * @return LoanResponse containing the created loan details
     * @throws BusinessRuleException if user cannot apply for loan (e.g., already has pending loan)
     */
    LoanResponse applyForLoan(String userEmail, ApplyLoanRequest request);

    /**
     * Approve or reject a loan application
     * Only admins can perform this action
     * 
     * @param loanId ID of the loan to approve/reject
     * @param request Approval decision and optional rejection reason
     * @return LoanResponse containing updated loan details
     * @throws ResourceNotFoundException if loan not found
     * @throws BusinessRuleException if loan cannot be approved (e.g., not in PENDING status)
     */
    LoanResponse approveLoan(String loanId, LoanApprovalRequest request);

    /**
     * Disburse approved loan by crediting user's wallet
     * Only loans in APPROVED status can be disbursed
     * 
     * @param loanId ID of the loan to disburse
     * @return LoanResponse containing updated loan details
     * @throws ResourceNotFoundException if loan not found
     * @throws BusinessRuleException if loan cannot be disbursed
     */
    LoanResponse disburseLoan(String loanId);

    /**
     * Repay a loan
     * User can make partial or full repayment
     * 
     * @param userEmail Email of the user repaying the loan
     * @param loanId ID of the loan to repay
     * @param request Repayment amount request
     * @return LoanResponse containing updated loan details
     * @throws ResourceNotFoundException if loan or user not found
     * @throws BusinessRuleException if repayment is invalid (e.g., amount exceeds remaining balance)
     */
    LoanResponse repayLoan(String userEmail, String loanId, LoanRepaymentRequest request);

    /**
     * Get loan details by ID
     * 
     * @param userEmail Email of the user requesting loan details
     * @param loanId ID of the loan
     * @return LoanResponse with loan details
     * @throws ResourceNotFoundException if loan not found
     * @throws BusinessRuleException if user is not authorized to view this loan
     */
    LoanResponse getLoanDetails(String userEmail, String loanId);

    /**
     * Get all loans for the current user
     * 
     * @param userEmail Email of the user
     * @return List of LoanResponse containing all user's loans
     */
    List<LoanResponse> getUserLoans(String userEmail);

    /**
     * Get all loans by status (Admin function)
     * 
     * @param status Loan status to filter by
     * @return List of LoanResponse with specified status
     */
    List<LoanResponse> getLoansByStatus(String status);

    /**
     * Get all loans in the system (Admin function)
     * 
     * @return List of all LoanResponse
     */
    List<LoanResponse> getAllLoans();
}


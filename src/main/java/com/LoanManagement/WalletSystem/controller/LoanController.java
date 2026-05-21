package com.LoanManagement.WalletSystem.controller;

import com.LoanManagement.WalletSystem.dto.Loan.ApplyLoanRequest;
import com.LoanManagement.WalletSystem.dto.Loan.LoanApprovalRequest;
import com.LoanManagement.WalletSystem.dto.Loan.LoanRepaymentRequest;
import com.LoanManagement.WalletSystem.dto.Loan.LoanResponse;
import com.LoanManagement.WalletSystem.service.LoanService;
import com.LoanManagement.WalletSystem.util.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/loans")
@Validated
public class LoanController {

    private final LoanService loanService;
    private final SecurityUtil securityUtil;

    public LoanController(LoanService loanService, SecurityUtil securityUtil) {
        this.loanService = loanService;
        this.securityUtil = securityUtil;
    }

    /**
     * Apply for a new loan
     * POST /api/loans/apply
     * Requires authentication
     */
    @PostMapping("/apply")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<LoanResponse> applyForLoan(@Valid @RequestBody ApplyLoanRequest request) {
        String userEmail = securityUtil.getCurrentUserEmail();
        LoanResponse response = loanService.applyForLoan(userEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Approve or reject a loan application
     * PUT /api/loans/{loanId}/approve
     * Requires admin role
     */
    @PutMapping("/{loanId}/approve")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<LoanResponse> approveLoan(
            @PathVariable String loanId,
            @Valid @RequestBody LoanApprovalRequest request
    ) {
        LoanResponse response = loanService.approveLoan(loanId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Disburse an approved loan
     * PUT /api/loans/{loanId}/disburse
     * Requires admin role
     */
    @PutMapping("/{loanId}/disburse")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<LoanResponse> disburseLoan(@PathVariable String loanId) {
        LoanResponse response = loanService.disburseLoan(loanId);
        return ResponseEntity.ok(response);
    }

    /**
     * Repay a loan
     * POST /api/loans/{loanId}/repay
     * Requires authentication
     */
    @PostMapping("/{loanId}/repay")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<LoanResponse> repayLoan(
            @PathVariable String loanId,
            @Valid @RequestBody LoanRepaymentRequest request
    ) {
        String userEmail = securityUtil.getCurrentUserEmail();
        LoanResponse response = loanService.repayLoan(userEmail, loanId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Get loan details
     * GET /api/loans/{loanId}
     * Requires authentication
     */
    @GetMapping("/{loanId}")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<LoanResponse> getLoanDetails(@PathVariable String loanId) {
        String userEmail = securityUtil.getCurrentUserEmail();
        LoanResponse response = loanService.getLoanDetails(userEmail, loanId);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all loans for the current user
     * GET /api/loans/my-loans
     * Requires authentication
     */
    @GetMapping("/my-loans")
    @PreAuthorize("hasRole('ROLE_USER')")
    public ResponseEntity<List<LoanResponse>> getMyLoans() {
        String userEmail = securityUtil.getCurrentUserEmail();
        List<LoanResponse> loans = loanService.getUserLoans(userEmail);
        return ResponseEntity.ok(loans);
    }

    /**
     * Get all loans by status (Admin function)
     * GET /api/loans/status/{status}
     * Requires admin role
     */
    @GetMapping("/status/{status}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<LoanResponse>> getLoansByStatus(@PathVariable String status) {
        List<LoanResponse> loans = loanService.getLoansByStatus(status);
        return ResponseEntity.ok(loans);
    }

    /**
     * Get all loans in the system (Admin function)
     * GET /api/loans/all
     * Requires admin role
     */
    @GetMapping("/all")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<List<LoanResponse>> getAllLoans() {
        List<LoanResponse> loans = loanService.getAllLoans();
        return ResponseEntity.ok(loans);
    }
}


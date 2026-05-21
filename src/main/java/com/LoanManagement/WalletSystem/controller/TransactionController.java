package com.LoanManagement.WalletSystem.controller;

import com.LoanManagement.WalletSystem.dto.Transaction.TransactionResponse;
import com.LoanManagement.WalletSystem.service.TransactionService;
import com.LoanManagement.WalletSystem.util.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Transaction endpoints
 * Handles HTTP requests related to transaction operations
 * All endpoints require JWT authentication
 */
@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final SecurityUtil securityUtil;

    public TransactionController(TransactionService transactionService, SecurityUtil securityUtil) {
        this.transactionService = transactionService;
        this.securityUtil = securityUtil;
    }

    /**
     * GET /api/transactions
     * Retrieve all transactions for the authenticated user
     * Transactions are returned in descending order by creation date
     *
     * @return List of transactions for the user
     */
    @GetMapping
    public ResponseEntity<List<TransactionResponse>> getAllTransactions() {
        String userEmail = securityUtil.getCurrentUserEmail();
        List<TransactionResponse> transactions = transactionService.getAllTransactions(userEmail);
        return ResponseEntity.ok(transactions);
    }

    /**
     * GET /api/transactions/{transactionId}
     * Retrieve a single transaction by ID
     * Only the transaction owner can access their transactions
     *
     * @param transactionId the ID of the transaction to retrieve
     * @return Transaction details
     */
    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponse> getTransactionById(@PathVariable String transactionId) {
        String userEmail = securityUtil.getCurrentUserEmail();
        TransactionResponse transaction = transactionService.getTransactionById(userEmail, transactionId);
        return ResponseEntity.ok(transaction);
    }
}


package com.LoanManagement.WalletSystem.service;

import com.LoanManagement.WalletSystem.dto.Transaction.TransactionResponse;
import java.util.List;

/**
 * Service interface for managing transactions
 * Defines operations for retrieving and querying transaction data
 */
public interface TransactionService {
    /**
     * Get all transactions for the currently authenticated user
     * @param userEmail the email of the authenticated user
     * @return list of transaction responses for the user
     */
    List<TransactionResponse> getAllTransactions(String userEmail);

    /**
     * Get a single transaction by ID if it belongs to the authenticated user
     * @param userEmail the email of the authenticated user
     * @param transactionId the ID of the transaction to retrieve
     * @return the transaction response
     */
    TransactionResponse getTransactionById(String userEmail, String transactionId);
}


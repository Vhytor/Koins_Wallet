package com.LoanManagement.WalletSystem.service.impl;

import com.LoanManagement.WalletSystem.dto.Transaction.TransactionResponse;
import com.LoanManagement.WalletSystem.exception.ResourceNotFoundException;
import com.LoanManagement.WalletSystem.mapper.TransactionMapper;
import com.LoanManagement.WalletSystem.model.Transaction;
import com.LoanManagement.WalletSystem.model.User;
import com.LoanManagement.WalletSystem.repository.TransactionRepository;
import com.LoanManagement.WalletSystem.repository.UserRepository;
import com.LoanManagement.WalletSystem.service.TransactionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final TransactionMapper transactionMapper;

    public TransactionServiceImpl(
            TransactionRepository transactionRepository,
            UserRepository userRepository,
            TransactionMapper transactionMapper
    ) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public List<TransactionResponse> getAllTransactions(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Retrieve all transactions for the user
        List<Transaction> transactions = transactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId());

        // Map to DTOs and return
        return transactionMapper.toTransactionResponseList(transactions);
    }

    @Override
    public TransactionResponse getTransactionById(String userEmail, String transactionId) {
        // Validate user exists
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Retrieve transaction and verify it belongs to the user
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found or does not belong to the authenticated user"));

        // Map to DTO and return
        return transactionMapper.toTransactionResponse(transaction);
    }


    List<Transaction> getTransactionsForWallet(String walletId) {
        return transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
    }
}


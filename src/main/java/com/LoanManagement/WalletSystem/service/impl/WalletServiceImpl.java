package com.LoanManagement.WalletSystem.service.impl;

import com.LoanManagement.WalletSystem.dto.Transaction.TransactionResponse;
import com.LoanManagement.WalletSystem.dto.Wallet.FundRequest;
import com.LoanManagement.WalletSystem.dto.Wallet.WalletResponse;
import com.LoanManagement.WalletSystem.exception.BusinessRuleException;
import com.LoanManagement.WalletSystem.exception.ResourceNotFoundException;
import com.LoanManagement.WalletSystem.mapper.TransactionMapper;
import com.LoanManagement.WalletSystem.mapper.WalletMapper;
import com.LoanManagement.WalletSystem.model.Transaction;
import com.LoanManagement.WalletSystem.model.TransactionType;
import com.LoanManagement.WalletSystem.model.User;
import com.LoanManagement.WalletSystem.model.Wallet;
import com.LoanManagement.WalletSystem.repository.TransactionRepository;
import com.LoanManagement.WalletSystem.repository.UserRepository;
import com.LoanManagement.WalletSystem.repository.WalletRepository;
import com.LoanManagement.WalletSystem.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final TransactionRepository transactionRepository;
    private final WalletMapper walletMapper;
    private final TransactionMapper transactionMapper;

    public WalletServiceImpl(
            WalletRepository walletRepository,
            UserRepository userRepository,
            TransactionRepository transactionRepository,
            WalletMapper walletMapper,
            TransactionMapper transactionMapper
    ) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.transactionRepository = transactionRepository;
        this.walletMapper = walletMapper;
        this.transactionMapper = transactionMapper;
    }

    @Override
    public WalletResponse getMyWallet(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found for user"));

        return walletMapper.toWalletResponse(wallet);
    }

    @Override
    @Transactional
    public TransactionResponse fundWallet(String userEmail, String walletId, FundRequest request) {
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get wallet
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        // Verify wallet belongs to user
        if (!wallet.getUser().getId().equals(user.getId())) {
            throw new BusinessRuleException("Wallet does not belong to the authenticated user");
        }

        // Validate amount
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Amount must be greater than zero");
        }

        // Create transaction
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setUserId(user.getId());
        transaction.setType(TransactionType.CREDIT);
        transaction.setAmount(request.getAmount());
        transaction.setReference(request.getReference());
        transaction.setStatus(1); // Success for simulated gateway

        // Update wallet balance
        BigDecimal newBalance = wallet.getBalance().add(request.getAmount());
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        // Save transaction
        Transaction savedTransaction = transactionRepository.save(transaction);

        return transactionMapper.toTransactionResponse(savedTransaction);
    }

    @Override
    public List<TransactionResponse> getTransactionHistory(String userEmail, String walletId) {
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get wallet
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

        // Verify wallet belongs to user
        if (!wallet.getUser().getId().equals(user.getId())) {
            throw new BusinessRuleException("Wallet does not belong to the authenticated user");
        }

        List<Transaction> transactions = transactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId);
        return transactionMapper.toTransactionResponseList(transactions);
    }
}


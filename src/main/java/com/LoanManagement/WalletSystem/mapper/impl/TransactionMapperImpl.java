package com.LoanManagement.WalletSystem.mapper.impl;

import com.LoanManagement.WalletSystem.dto.Transaction.TransactionResponse;
import com.LoanManagement.WalletSystem.mapper.TransactionMapper;
import com.LoanManagement.WalletSystem.model.Transaction;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class TransactionMapperImpl implements TransactionMapper {

    @Override
    public TransactionResponse toTransactionResponse(Transaction transaction) {
        if (transaction == null) {
            return null;
        }
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setType(transaction.getType() != null ? transaction.getType().name() : null);
        response.setAmount(transaction.getAmount());
        response.setReference(transaction.getReference());
        response.setStatus(transaction.getStatus());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }

    @Override
    public List<TransactionResponse> toTransactionResponseList(List<Transaction> transactions) {
        if (transactions == null || transactions.isEmpty()) {
            return List.of();
        }
        return transactions.stream()
                .map(this::toTransactionResponse)
                .collect(Collectors.toList());
    }
}


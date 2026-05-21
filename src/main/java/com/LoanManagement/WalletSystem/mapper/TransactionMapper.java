package com.LoanManagement.WalletSystem.mapper;

import com.LoanManagement.WalletSystem.dto.Transaction.TransactionResponse;
import com.LoanManagement.WalletSystem.model.Transaction;
import java.util.List;

public interface TransactionMapper {
    TransactionResponse toTransactionResponse(Transaction transaction);
    List<TransactionResponse> toTransactionResponseList(List<Transaction> transactions);
}


package com.LoanManagement.WalletSystem.service;

import com.LoanManagement.WalletSystem.dto.Transaction.TransactionResponse;
import com.LoanManagement.WalletSystem.dto.Wallet.FundRequest;
import com.LoanManagement.WalletSystem.dto.Wallet.WalletResponse;

import java.util.List;

public interface WalletService {
    /**
     * Get the wallet for the currently authenticated user
     */
    WalletResponse getMyWallet(String userEmail);

    /**
     * Fund wallet for currently authenticated user
     */
    TransactionResponse fundWallet(String userEmail, String walletId, FundRequest request);

    /**
     * Get transaction history for a wallet
     */
    List<TransactionResponse> getTransactionHistory(String userEmail, String walletId);
}


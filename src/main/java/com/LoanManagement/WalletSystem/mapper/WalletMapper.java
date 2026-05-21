package com.LoanManagement.WalletSystem.mapper;

import com.LoanManagement.WalletSystem.dto.Wallet.WalletResponse;
import com.LoanManagement.WalletSystem.model.Wallet;

public interface WalletMapper {
    WalletResponse toWalletResponse(Wallet wallet);
}


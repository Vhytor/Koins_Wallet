package com.LoanManagement.WalletSystem.mapper.impl;

import com.LoanManagement.WalletSystem.dto.Wallet.WalletResponse;
import com.LoanManagement.WalletSystem.mapper.WalletMapper;
import com.LoanManagement.WalletSystem.model.Wallet;
import org.springframework.stereotype.Component;

@Component
public class WalletMapperImpl implements WalletMapper {

    @Override
    public WalletResponse toWalletResponse(Wallet wallet) {
        if (wallet == null) {
            return null;
        }
        return new WalletResponse(
                wallet.getId(),
                wallet.getUser() != null ? wallet.getUser().getId() : null,
                wallet.getBalance(),
                wallet.getCurrency(),
                wallet.getStatus(),
                wallet.getCreatedAt()
        );
    }
}


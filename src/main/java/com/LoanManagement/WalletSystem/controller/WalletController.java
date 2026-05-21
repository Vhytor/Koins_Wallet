package com.LoanManagement.WalletSystem.controller;

import com.LoanManagement.WalletSystem.dto.Transaction.TransactionResponse;
import com.LoanManagement.WalletSystem.dto.Wallet.FundRequest;
import com.LoanManagement.WalletSystem.dto.Wallet.WalletResponse;
import com.LoanManagement.WalletSystem.service.WalletService;
import com.LoanManagement.WalletSystem.util.SecurityUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    private final WalletService walletService;
    private final SecurityUtil securityUtil;

    public WalletController(WalletService walletService, SecurityUtil securityUtil) {
        this.walletService = walletService;
        this.securityUtil = securityUtil;
    }

    @GetMapping("/me")
    public ResponseEntity<WalletResponse> getMyWallet() {
        String userEmail = securityUtil.getCurrentUserEmail();
        WalletResponse wallet = walletService.getMyWallet(userEmail);
        return ResponseEntity.ok(wallet);
    }

    @PostMapping("/{walletId}/fund")
    public ResponseEntity<TransactionResponse> fundWallet(
            @PathVariable String walletId,
            @Valid @RequestBody FundRequest request
    ) {
        String userEmail = securityUtil.getCurrentUserEmail();
        TransactionResponse transaction = walletService.fundWallet(userEmail, walletId, request);
        return ResponseEntity.ok(transaction);
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<List<TransactionResponse>> getTransactionHistory(
            @PathVariable String walletId
    ) {
        String userEmail = securityUtil.getCurrentUserEmail();
        List<TransactionResponse> transactions = walletService.getTransactionHistory(userEmail, walletId);
        return ResponseEntity.ok(transactions);
    }
}


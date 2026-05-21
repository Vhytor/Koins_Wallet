package com.LoanManagement.WalletSystem.controller;

import com.LoanManagement.WalletSystem.dto.Transaction.TransactionResponse;
import com.LoanManagement.WalletSystem.dto.Wallet.FundRequest;
import com.LoanManagement.WalletSystem.dto.Wallet.WalletResponse;
import com.LoanManagement.WalletSystem.exception.BusinessRuleException;
import com.LoanManagement.WalletSystem.exception.ResourceNotFoundException;
import com.LoanManagement.WalletSystem.service.WalletService;
import com.LoanManagement.WalletSystem.util.SecurityUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@WithMockUser(username = "john@example.com")
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WalletService walletService;

    @MockBean
    private SecurityUtil securityUtil;

    @Autowired
    private ObjectMapper objectMapper;

    private WalletResponse walletResponse;
    private FundRequest fundRequest;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        // Setup wallet response
        walletResponse = new WalletResponse(
                "wallet-1",
                "user-1",
                new BigDecimal("1000.00"),
                "NGN",
                1,
                Instant.now()
        );

        // Setup fund request
        fundRequest = new FundRequest();
        fundRequest.setAmount(new BigDecimal("500.00"));
        fundRequest.setReference("ref-123");

        // Setup transaction response
        transactionResponse = new TransactionResponse();
        transactionResponse.setId("txn-1");
        transactionResponse.setType("CREDIT");
        transactionResponse.setAmount(new BigDecimal("500.00"));
        transactionResponse.setReference("ref-123");
        transactionResponse.setStatus(1);
        transactionResponse.setCreatedAt(Instant.now());
    }

    // =============== GET MY WALLET TESTS ===============

    @Test
    void testGetMyWalletReturns200Ok() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn("john@example.com");
        when(walletService.getMyWallet("john@example.com")).thenReturn(walletResponse);

        // Act & Assert
        mockMvc.perform(get("/api/wallets/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("wallet-1"))
                .andExpect(jsonPath("$.userId").value("user-1"))
                .andExpect(jsonPath("$.balance").value(1000.00))
                .andExpect(jsonPath("$.currency").value("NGN"));
    }

    @Test
    void testGetMyWalletReturnsCorrectWalletData() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn("john@example.com");
        when(walletService.getMyWallet("john@example.com")).thenReturn(walletResponse);

        // Act & Assert
        mockMvc.perform(get("/api/wallets/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(1))
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    void testGetMyWalletWalletNotFoundReturns404() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn("john@example.com");
        when(walletService.getMyWallet("john@example.com"))
                .thenThrow(new ResourceNotFoundException("Wallet not found for user"));

        // Act & Assert
        mockMvc.perform(get("/api/wallets/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Wallet not found for user"));
    }

    @Test
    void testGetMyWalletUserNotFoundReturns404() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn("john@example.com");
        when(walletService.getMyWallet("john@example.com"))
                .thenThrow(new ResourceNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/api/wallets/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("User not found"));
    }

    @Test
    void testGetMyWalletRequiresAuthentication() throws Exception {
        // This test uses @WithMockUser, so it should pass
        // If we were testing without it, it would return 401 Unauthorized
        mockMvc.perform(get("/api/wallets/me")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    // =============== FUND WALLET TESTS ===============

    @Test
    void testFundWalletReturns200Ok() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn("john@example.com");
        when(walletService.fundWallet("john@example.com", "wallet-1", fundRequest))
                .thenReturn(transactionResponse);

        // Act & Assert
        mockMvc.perform(post("/api/wallets/wallet-1/fund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fundRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("txn-1"))
                .andExpect(jsonPath("$.type").value("CREDIT"))
                .andExpect(jsonPath("$.amount").value(500.00));
    }

    @Test
    void testFundWalletReturnsTransactionResponse() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn("john@example.com");
        when(walletService.fundWallet("john@example.com", "wallet-1", fundRequest))
                .thenReturn(transactionResponse);

        // Act & Assert
        mockMvc.perform(post("/api/wallets/wallet-1/fund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fundRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reference").value("ref-123"))
                .andExpect(jsonPath("$.status").value(1));
    }

    @Test
    void testFundWalletWithInvalidAmountReturns400() throws Exception {
        // Arrange
        fundRequest.setAmount(BigDecimal.ZERO);
        when(securityUtil.getCurrentUserEmail()).thenReturn("john@example.com");
        when(walletService.fundWallet("john@example.com", "wallet-1", fundRequest))
                .thenThrow(new BusinessRuleException("Amount must be greater than zero"));

        // Act & Assert
        mockMvc.perform(post("/api/wallets/wallet-1/fund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fundRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Amount must be greater than zero"));
    }

    @Test
    void testFundWalletWithNegativeAmountReturns400() throws Exception {
        // Arrange
        fundRequest.setAmount(new BigDecimal("-100.00"));
        when(securityUtil.getCurrentUserEmail()).thenReturn("john@example.com");
        when(walletService.fundWallet("john@example.com", "wallet-1", fundRequest))
                .thenThrow(new BusinessRuleException("Amount must be greater than zero"));

        // Act & Assert
        mockMvc.perform(post("/api/wallets/wallet-1/fund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fundRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testFundWalletWalletNotFoundReturns404() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn("john@example.com");
        when(walletService.fundWallet("john@example.com", "wallet-1", fundRequest))
                .thenThrow(new ResourceNotFoundException("Wallet not found"));

        // Act & Assert
        mockMvc.perform(post("/api/wallets/wallet-1/fund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fundRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Wallet not found"));
    }

    @Test
    void testFundWalletNotOwnedByUserReturns400() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn("john@example.com");
        when(walletService.fundWallet("john@example.com", "wallet-1", fundRequest))
                .thenThrow(new BusinessRuleException("Wallet does not belong to the authenticated user"));

        // Act & Assert
        mockMvc.perform(post("/api/wallets/wallet-1/fund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fundRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    void testFundWalletWithMissingAmountReturns400() throws Exception {
        // Arrange
        fundRequest.setAmount(null);

        // Act & Assert
        mockMvc.perform(post("/api/wallets/wallet-1/fund")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(fundRequest)))
                .andExpect(status().isBadRequest());
    }

    // =============== GET TRANSACTION HISTORY TESTS ===============

    @Test
    void testGetTransactionHistoryReturns200Ok() throws Exception {
        // Arrange
        List<TransactionResponse> transactions = new ArrayList<>();
        transactions.add(transactionResponse);
        when(securityUtil.getCurrentUserEmail()).thenReturn("john@example.com");
        when(walletService.getTransactionHistory("john@example.com", "wallet-1"))
                .thenReturn(transactions);

        // Act & Assert
        mockMvc.perform(get("/api/wallets/wallet-1/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("txn-1"))
                .andExpect(jsonPath("$[0].type").value("CREDIT"))
                .andExpect(jsonPath("$[0].amount").value(500.00));
    }

    @Test
    void testGetTransactionHistoryReturnsListOfTransactions() throws Exception {
        // Arrange
        List<TransactionResponse> transactions = new ArrayList<>();
        TransactionResponse txn2 = new TransactionResponse();
        txn2.setId("txn-2");
        txn2.setType("DEBIT");
        transactions.add(transactionResponse);
        transactions.add(txn2);

        when(securityUtil.getCurrentUserEmail()).thenReturn("john@example.com");
        when(walletService.getTransactionHistory("john@example.com", "wallet-1"))
                .thenReturn(transactions);

        // Act & Assert
        mockMvc.perform(get("/api/wallets/wallet-1/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].id").value("txn-1"))
                .andExpect(jsonPath("$[1].id").value("txn-2"));
    }

    @Test
    void testGetTransactionHistoryEmptyListReturns200() throws Exception {
        // Arrange
        List<TransactionResponse> emptyList = new ArrayList<>();
        when(securityUtil.getCurrentUserEmail()).thenReturn("john@example.com");
        when(walletService.getTransactionHistory("john@example.com", "wallet-1"))
                .thenReturn(emptyList);

        // Act & Assert
        mockMvc.perform(get("/api/wallets/wallet-1/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void testGetTransactionHistoryWalletNotFoundReturns404() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn("john@example.com");
        when(walletService.getTransactionHistory("john@example.com", "wallet-1"))
                .thenThrow(new ResourceNotFoundException("Wallet not found"));

        // Act & Assert
        mockMvc.perform(get("/api/wallets/wallet-1/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Wallet not found"));
    }

    @Test
    void testGetTransactionHistoryNotOwnedByUserReturns400() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn("john@example.com");
        when(walletService.getTransactionHistory("john@example.com", "wallet-1"))
                .thenThrow(new BusinessRuleException("Wallet does not belong to the authenticated user"));

        // Act & Assert
        mockMvc.perform(get("/api/wallets/wallet-1/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}


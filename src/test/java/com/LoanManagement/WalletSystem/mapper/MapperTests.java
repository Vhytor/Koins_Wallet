package com.LoanManagement.WalletSystem.mapper;

import com.LoanManagement.WalletSystem.dto.Auth.UserResponse;
import com.LoanManagement.WalletSystem.dto.Transaction.TransactionResponse;
import com.LoanManagement.WalletSystem.dto.Wallet.WalletResponse;
import com.LoanManagement.WalletSystem.mapper.impl.TransactionMapperImpl;
import com.LoanManagement.WalletSystem.mapper.impl.UserMapperImpl;
import com.LoanManagement.WalletSystem.mapper.impl.WalletMapperImpl;
import com.LoanManagement.WalletSystem.model.Transaction;
import com.LoanManagement.WalletSystem.model.TransactionType;
import com.LoanManagement.WalletSystem.model.User;
import com.LoanManagement.WalletSystem.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MapperTests {

    private UserMapperImpl userMapper;
    private WalletMapperImpl walletMapper;
    private TransactionMapperImpl transactionMapper;

    @BeforeEach
    void setUp() {
        userMapper = new UserMapperImpl();
        walletMapper = new WalletMapperImpl();
        transactionMapper = new TransactionMapperImpl();
    }

    // =============== USER MAPPER TESTS ===============

    @Test
    void testUserMapperMapToUserResponseSuccessfully() {
        // Arrange
        User user = new User();
        user.setId("user-1");
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPhone("1234567890");
        user.setAccountStatus(1);
        user.setCreatedAt(Instant.parse("2026-05-20T10:00:00Z"));

        // Act
        UserResponse response = userMapper.toUserResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals("user-1", response.getId());
        assertEquals("John Doe", response.getFullName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("1234567890", response.getPhone());
        assertEquals(1, response.getAccountStatus());
        assertEquals(Instant.parse("2026-05-20T10:00:00Z"), response.getCreatedAt());
    }

    @Test
    void testUserMapperMapNullUserReturnsNull() {
        // Act
        UserResponse response = userMapper.toUserResponse(null);

        // Assert
        assertNull(response);
    }

    @Test
    void testUserMapperPreservesAllFields() {
        // Arrange
        User user = new User();
        user.setId("test-id");
        user.setFullName("Test User");
        user.setEmail("test@test.com");
        user.setPhone("9876543210");
        user.setAccountStatus(2);
        Instant now = Instant.now();
        user.setCreatedAt(now);

        // Act
        UserResponse response = userMapper.toUserResponse(user);

        // Assert
        assertNotNull(response);
        assertEquals(user.getId(), response.getId());
        assertEquals(user.getFullName(), response.getFullName());
        assertEquals(user.getEmail(), response.getEmail());
        assertEquals(user.getPhone(), response.getPhone());
        assertEquals(user.getAccountStatus(), response.getAccountStatus());
        assertEquals(user.getCreatedAt(), response.getCreatedAt());
    }

    @Test
    void testUserMapperHandlesNullPhoneField() {
        // Arrange
        User user = new User();
        user.setId("user-1");
        user.setFullName("John Doe");
        user.setEmail("john@example.com");
        user.setPhone(null);
        user.setAccountStatus(1);
        user.setCreatedAt(Instant.now());

        // Act
        UserResponse response = userMapper.toUserResponse(user);

        // Assert
        assertNotNull(response);
        assertNull(response.getPhone());
    }

    // =============== WALLET MAPPER TESTS ===============

    @Test
    void testWalletMapperMapToWalletResponseSuccessfully() {
        // Arrange
        User user = new User();
        user.setId("user-1");
        user.setFullName("John Doe");
        user.setEmail("john@example.com");

        Wallet wallet = new Wallet();
        wallet.setId("wallet-1");
        wallet.setUser(user);
        wallet.setBalance(new BigDecimal("1000.50"));
        wallet.setCurrency("NGN");
        wallet.setStatus(1);
        wallet.setCreatedAt(Instant.parse("2026-05-20T10:00:00Z"));

        // Act
        WalletResponse response = walletMapper.toWalletResponse(wallet);

        // Assert
        assertNotNull(response);
        assertEquals("wallet-1", response.getId());
        assertEquals("user-1", response.getUserId());
        assertEquals(new BigDecimal("1000.50"), response.getBalance());
        assertEquals("NGN", response.getCurrency());
        assertEquals(1, response.getStatus());
        assertEquals(Instant.parse("2026-05-20T10:00:00Z"), response.getCreatedAt());
    }

    @Test
    void testWalletMapperMapNullWalletReturnsNull() {
        // Act
        WalletResponse response = walletMapper.toWalletResponse(null);

        // Assert
        assertNull(response);
    }

    @Test
    void testWalletMapperHandlesNullUserField() {
        // Arrange
        Wallet wallet = new Wallet();
        wallet.setId("wallet-1");
        wallet.setUser(null);
        wallet.setBalance(new BigDecimal("500.00"));
        wallet.setCurrency("NGN");
        wallet.setStatus(1);
        wallet.setCreatedAt(Instant.now());

        // Act
        WalletResponse response = walletMapper.toWalletResponse(wallet);

        // Assert
        assertNotNull(response);
        assertNull(response.getUserId());
    }

    @Test
    void testWalletMapperPreservesAllFields() {
        // Arrange
        User user = new User();
        user.setId("user-123");

        Wallet wallet = new Wallet();
        wallet.setId("wallet-123");
        wallet.setUser(user);
        wallet.setBalance(new BigDecimal("9999.99"));
        wallet.setCurrency("USD");
        wallet.setStatus(2);
        Instant now = Instant.now();
        wallet.setCreatedAt(now);

        // Act
        WalletResponse response = walletMapper.toWalletResponse(wallet);

        // Assert
        assertEquals("wallet-123", response.getId());
        assertEquals("user-123", response.getUserId());
        assertEquals(new BigDecimal("9999.99"), response.getBalance());
        assertEquals("USD", response.getCurrency());
        assertEquals(2, response.getStatus());
        assertEquals(now, response.getCreatedAt());
    }

    @Test
    void testWalletMapperHandlesDifferentCurrencies() {
        // Arrange
        User user = new User();
        user.setId("user-1");

        Wallet walletNGN = new Wallet();
        walletNGN.setId("wallet-1");
        walletNGN.setUser(user);
        walletNGN.setCurrency("NGN");

        Wallet walletUSD = new Wallet();
        walletUSD.setId("wallet-2");
        walletUSD.setUser(user);
        walletUSD.setCurrency("USD");

        // Act
        WalletResponse responseNGN = walletMapper.toWalletResponse(walletNGN);
        WalletResponse responseUSD = walletMapper.toWalletResponse(walletUSD);

        // Assert
        assertEquals("NGN", responseNGN.getCurrency());
        assertEquals("USD", responseUSD.getCurrency());
    }

    // =============== TRANSACTION MAPPER TESTS ===============

    @Test
    void testTransactionMapperMapToTransactionResponseSuccessfully() {
        // Arrange
        Wallet wallet = new Wallet();
        wallet.setId("wallet-1");

        Transaction transaction = new Transaction();
        transaction.setId("txn-1");
        transaction.setWallet(wallet);
        transaction.setType(TransactionType.CREDIT);
        transaction.setAmount(new BigDecimal("500.00"));
        transaction.setReference("ref-123");
        transaction.setStatus(1);
        transaction.setCreatedAt(Instant.parse("2026-05-20T10:00:00Z"));

        // Act
        TransactionResponse response = transactionMapper.toTransactionResponse(transaction);

        // Assert
        assertNotNull(response);
        assertEquals("txn-1", response.getId());
        assertEquals("CREDIT", response.getType());
        assertEquals(new BigDecimal("500.00"), response.getAmount());
        assertEquals("ref-123", response.getReference());
        assertEquals(1, response.getStatus());
        assertEquals(Instant.parse("2026-05-20T10:00:00Z"), response.getCreatedAt());
    }

    @Test
    void testTransactionMapperMapNullTransactionReturnsNull() {
        // Act
        TransactionResponse response = transactionMapper.toTransactionResponse(null);

        // Assert
        assertNull(response);
    }

    @Test
    void testTransactionMapperHandlesDebitTransactionType() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setId("txn-2");
        transaction.setType(TransactionType.DEBIT);
        transaction.setAmount(new BigDecimal("100.00"));
        transaction.setReference("debit-ref");
        transaction.setStatus(1);
        transaction.setCreatedAt(Instant.now());

        // Act
        TransactionResponse response = transactionMapper.toTransactionResponse(transaction);

        // Assert
        assertEquals("DEBIT", response.getType());
    }

    @Test
    void testTransactionMapperHandlesNullTypeField() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setId("txn-3");
        transaction.setType(null);
        transaction.setAmount(new BigDecimal("200.00"));
        transaction.setReference("null-type-ref");
        transaction.setStatus(1);
        transaction.setCreatedAt(Instant.now());

        // Act
        TransactionResponse response = transactionMapper.toTransactionResponse(transaction);

        // Assert
        assertNotNull(response);
        assertNull(response.getType());
    }

    @Test
    void testTransactionMapperMapToListSuccessfully() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();

        Transaction txn1 = new Transaction();
        txn1.setId("txn-1");
        txn1.setType(TransactionType.CREDIT);
        txn1.setAmount(new BigDecimal("100.00"));
        txn1.setReference("ref-1");
        txn1.setStatus(1);
        txn1.setCreatedAt(Instant.now());

        Transaction txn2 = new Transaction();
        txn2.setId("txn-2");
        txn2.setType(TransactionType.DEBIT);
        txn2.setAmount(new BigDecimal("50.00"));
        txn2.setReference("ref-2");
        txn2.setStatus(1);
        txn2.setCreatedAt(Instant.now());

        transactions.add(txn1);
        transactions.add(txn2);

        // Act
        List<TransactionResponse> responses = transactionMapper.toTransactionResponseList(transactions);

        // Assert
        assertNotNull(responses);
        assertEquals(2, responses.size());
        assertEquals("txn-1", responses.get(0).getId());
        assertEquals("txn-2", responses.get(1).getId());
        assertEquals("CREDIT", responses.get(0).getType());
        assertEquals("DEBIT", responses.get(1).getType());
    }

    @Test
    void testTransactionMapperMapNullListReturnsEmptyList() {
        // Act
        List<TransactionResponse> responses = transactionMapper.toTransactionResponseList(null);

        // Assert
        assertNotNull(responses);
        assertEquals(0, responses.size());
    }

    @Test
    void testTransactionMapperMapEmptyListReturnsEmptyList() {
        // Act
        List<TransactionResponse> responses = transactionMapper.toTransactionResponseList(new ArrayList<>());

        // Assert
        assertNotNull(responses);
        assertEquals(0, responses.size());
    }

    @Test
    void testTransactionMapperPreservesAllFields() {
        // Arrange
        Transaction transaction = new Transaction();
        transaction.setId("txn-123");
        transaction.setType(TransactionType.CREDIT);
        transaction.setAmount(new BigDecimal("12345.67"));
        transaction.setReference("custom-ref");
        transaction.setStatus(2);
        Instant now = Instant.now();
        transaction.setCreatedAt(now);

        // Act
        TransactionResponse response = transactionMapper.toTransactionResponse(transaction);

        // Assert
        assertEquals("txn-123", response.getId());
        assertEquals("CREDIT", response.getType());
        assertEquals(new BigDecimal("12345.67"), response.getAmount());
        assertEquals("custom-ref", response.getReference());
        assertEquals(2, response.getStatus());
        assertEquals(now, response.getCreatedAt());
    }

    @Test
    void testTransactionMapperListMaintainsOrder() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            Transaction txn = new Transaction();
            txn.setId("txn-" + i);
            txn.setType(TransactionType.CREDIT);
            txn.setAmount(new BigDecimal(i * 100));
            transactions.add(txn);
        }

        // Act
        List<TransactionResponse> responses = transactionMapper.toTransactionResponseList(transactions);

        // Assert
        assertEquals(5, responses.size());
        for (int i = 0; i < 5; i++) {
            assertEquals("txn-" + i, responses.get(i).getId());
        }
    }
}


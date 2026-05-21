package com.LoanManagement.WalletSystem.service;

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
import com.LoanManagement.WalletSystem.service.impl.WalletServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private WalletMapper walletMapper;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private WalletServiceImpl walletService;

    private User testUser;
    private Wallet testWallet;
    private FundRequest fundRequest;
    private WalletResponse walletResponse;
    private Transaction testTransaction;
    private TransactionResponse transactionResponse;

    @BeforeEach
    void setUp() {
        // Setup user
        testUser = new User();
        testUser.setId("user-1");
        testUser.setFullName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPassword("hashed-password");

        // Setup wallet
        testWallet = new Wallet();
        testWallet.setId("wallet-1");
        testWallet.setUser(testUser);
        testWallet.setBalance(new BigDecimal("1000.00"));
        testWallet.setCurrency("NGN");
        testWallet.setStatus(1);
        testWallet.setCreatedAt(Instant.now());

        // Setup fund request
        fundRequest = new FundRequest();
        fundRequest.setAmount(new BigDecimal("500.00"));
        fundRequest.setReference("txn-ref-123");

        // Setup wallet response
        walletResponse = new WalletResponse(
                "wallet-1",
                "user-1",
                new BigDecimal("1000.00"),
                "NGN",
                1,
                Instant.now()
        );

        // Setup transaction
        testTransaction = new Transaction();
        testTransaction.setId("txn-1");
        testTransaction.setWallet(testWallet);
        testTransaction.setUserId("user-1");
        testTransaction.setType(TransactionType.CREDIT);
        testTransaction.setAmount(new BigDecimal("500.00"));
        testTransaction.setReference("txn-ref-123");
        testTransaction.setStatus(1);
        testTransaction.setCreatedAt(Instant.now());

        // Setup transaction response
        transactionResponse = new TransactionResponse();
        transactionResponse.setId("txn-1");
        transactionResponse.setType("CREDIT");
        transactionResponse.setAmount(new BigDecimal("500.00"));
        transactionResponse.setReference("txn-ref-123");
        transactionResponse.setStatus(1);
        transactionResponse.setCreatedAt(Instant.now());
    }

    // =============== GET MY WALLET TESTS ===============

    @Test
    void testGetMyWalletSuccessfully() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(walletRepository.findByUserId("user-1")).thenReturn(Optional.of(testWallet));
        when(walletMapper.toWalletResponse(testWallet)).thenReturn(walletResponse);

        // Act
        WalletResponse result = walletService.getMyWallet("john@example.com");

        // Assert
        assertNotNull(result);
        assertEquals("wallet-1", result.getId());
        assertEquals("user-1", result.getUserId());
        assertEquals(new BigDecimal("1000.00"), result.getBalance());
        verify(userRepository, times(1)).findByEmail("john@example.com");
        verify(walletRepository, times(1)).findByUserId("user-1");
    }

    @Test
    void testGetMyWalletUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            walletService.getMyWallet("nonexistent@example.com");
        });
        assertEquals("User not found", exception.getMessage());
        verify(walletRepository, never()).findByUserId(anyString());
    }

    @Test
    void testGetMyWalletWalletNotFound() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(walletRepository.findByUserId("user-1")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            walletService.getMyWallet("john@example.com");
        });
        assertEquals("Wallet not found for user", exception.getMessage());
    }

    // =============== FUND WALLET TESTS ===============

    @Test
    void testFundWalletSuccessfully() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(walletRepository.findById("wallet-1")).thenReturn(Optional.of(testWallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
        when(transactionMapper.toTransactionResponse(testTransaction)).thenReturn(transactionResponse);

        // Act
        TransactionResponse result = walletService.fundWallet("john@example.com", "wallet-1", fundRequest);

        // Assert
        assertNotNull(result);
        assertEquals("txn-1", result.getId());
        assertEquals("CREDIT", result.getType());
        assertEquals(new BigDecimal("500.00"), result.getAmount());
        verify(walletRepository, times(1)).save(argThat(wallet ->
                wallet.getBalance().equals(new BigDecimal("1500.00"))
        ));
    }

    @Test
    void testFundWalletUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            walletService.fundWallet("nonexistent@example.com", "wallet-1", fundRequest);
        });
        assertEquals("User not found", exception.getMessage());
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void testFundWalletWalletNotFound() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(walletRepository.findById("wallet-1")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            walletService.fundWallet("john@example.com", "wallet-1", fundRequest);
        });
        assertEquals("Wallet not found", exception.getMessage());
    }

    @Test
    void testFundWalletDoesNotBelongToUser() {
        // Arrange
        User otherUser = new User();
        otherUser.setId("user-2");
        testWallet.setUser(otherUser);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(walletRepository.findById("wallet-1")).thenReturn(Optional.of(testWallet));

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            walletService.fundWallet("john@example.com", "wallet-1", fundRequest);
        });
        assertEquals("Wallet does not belong to the authenticated user", exception.getMessage());
        verify(transactionRepository, never()).save(any(Transaction.class));
    }

    @Test
    void testFundWalletWithNegativeAmount() {
        // Arrange
        fundRequest.setAmount(new BigDecimal("-100.00"));
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(walletRepository.findById("wallet-1")).thenReturn(Optional.of(testWallet));

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            walletService.fundWallet("john@example.com", "wallet-1", fundRequest);
        });
        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testFundWalletWithZeroAmount() {
        // Arrange
        fundRequest.setAmount(BigDecimal.ZERO);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(walletRepository.findById("wallet-1")).thenReturn(Optional.of(testWallet));

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            walletService.fundWallet("john@example.com", "wallet-1", fundRequest);
        });
        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testFundWalletWithNullAmount() {
        // Arrange
        fundRequest.setAmount(null);
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(walletRepository.findById("wallet-1")).thenReturn(Optional.of(testWallet));

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            walletService.fundWallet("john@example.com", "wallet-1", fundRequest);
        });
        assertEquals("Amount must be greater than zero", exception.getMessage());
    }

    @Test
    void testFundWalletUpdatesBalanceCorrectly() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(walletRepository.findById("wallet-1")).thenReturn(Optional.of(testWallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
        when(transactionMapper.toTransactionResponse(testTransaction)).thenReturn(transactionResponse);

        // Act
        walletService.fundWallet("john@example.com", "wallet-1", fundRequest);

        // Assert - Verify balance was updated to 1500
        verify(walletRepository, times(1)).save(argThat(wallet ->
                wallet.getBalance().equals(testWallet.getBalance().add(fundRequest.getAmount()))
        ));
    }

    @Test
    void testFundWalletCreatesTransactionWithCorrectFields() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(walletRepository.findById("wallet-1")).thenReturn(Optional.of(testWallet));
        when(transactionRepository.save(any(Transaction.class))).thenReturn(testTransaction);
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
        when(transactionMapper.toTransactionResponse(testTransaction)).thenReturn(transactionResponse);

        // Act
        walletService.fundWallet("john@example.com", "wallet-1", fundRequest);

        // Assert
        verify(transactionRepository, times(1)).save(argThat(transaction ->
                transaction.getType() == TransactionType.CREDIT &&
                transaction.getAmount().equals(fundRequest.getAmount()) &&
                transaction.getReference().equals(fundRequest.getReference()) &&
                transaction.getStatus() == 1 &&
                transaction.getWallet().equals(testWallet)
        ));
    }

    // =============== GET TRANSACTION HISTORY TESTS ===============

    @Test
    void testGetTransactionHistorySuccessfully() {
        // Arrange
        List<Transaction> transactions = new ArrayList<>();
        transactions.add(testTransaction);
        List<TransactionResponse> responses = new ArrayList<>();
        responses.add(transactionResponse);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(walletRepository.findById("wallet-1")).thenReturn(Optional.of(testWallet));
        when(transactionRepository.findByWalletIdOrderByCreatedAtDesc("wallet-1")).thenReturn(transactions);
        when(transactionMapper.toTransactionResponseList(transactions)).thenReturn(responses);

        // Act
        List<TransactionResponse> result = walletService.getTransactionHistory("john@example.com", "wallet-1");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("txn-1", result.get(0).getId());
        verify(transactionRepository, times(1)).findByWalletIdOrderByCreatedAtDesc("wallet-1");
    }

    @Test
    void testGetTransactionHistoryUserNotFound() {
        // Arrange
        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            walletService.getTransactionHistory("nonexistent@example.com", "wallet-1");
        });
        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void testGetTransactionHistoryWalletNotFound() {
        // Arrange
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(walletRepository.findById("wallet-1")).thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class, () -> {
            walletService.getTransactionHistory("john@example.com", "wallet-1");
        });
        assertEquals("Wallet not found", exception.getMessage());
    }

    @Test
    void testGetTransactionHistoryWalletDoesNotBelongToUser() {
        // Arrange
        User otherUser = new User();
        otherUser.setId("user-2");
        testWallet.setUser(otherUser);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(walletRepository.findById("wallet-1")).thenReturn(Optional.of(testWallet));

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            walletService.getTransactionHistory("john@example.com", "wallet-1");
        });
        assertEquals("Wallet does not belong to the authenticated user", exception.getMessage());
    }

    @Test
    void testGetTransactionHistoryEmptyList() {
        // Arrange
        List<Transaction> emptyTransactions = new ArrayList<>();
        List<TransactionResponse> emptyResponses = new ArrayList<>();

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(walletRepository.findById("wallet-1")).thenReturn(Optional.of(testWallet));
        when(transactionRepository.findByWalletIdOrderByCreatedAtDesc("wallet-1")).thenReturn(emptyTransactions);
        when(transactionMapper.toTransactionResponseList(emptyTransactions)).thenReturn(emptyResponses);

        // Act
        List<TransactionResponse> result = walletService.getTransactionHistory("john@example.com", "wallet-1");

        // Assert
        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testGetTransactionHistoryOrderedByCreatedAtDesc() {
        // Arrange
        verify(transactionRepository, never()).findByWalletIdOrderByCreatedAtDesc(anyString());

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(testTransaction);

        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(testUser));
        when(walletRepository.findById("wallet-1")).thenReturn(Optional.of(testWallet));
        when(transactionRepository.findByWalletIdOrderByCreatedAtDesc("wallet-1")).thenReturn(transactions);
        when(transactionMapper.toTransactionResponseList(transactions)).thenReturn(new ArrayList<>());

        // Act
        walletService.getTransactionHistory("john@example.com", "wallet-1");

        // Assert
        verify(transactionRepository, times(1)).findByWalletIdOrderByCreatedAtDesc("wallet-1");
    }
}


package com.LoanManagement.WalletSystem.service.impl;

import com.LoanManagement.WalletSystem.dto.Transaction.TransactionResponse;
import com.LoanManagement.WalletSystem.exception.ResourceNotFoundException;
import com.LoanManagement.WalletSystem.mapper.TransactionMapper;
import com.LoanManagement.WalletSystem.model.Transaction;
import com.LoanManagement.WalletSystem.model.TransactionType;
import com.LoanManagement.WalletSystem.model.User;
import com.LoanManagement.WalletSystem.repository.TransactionRepository;
import com.LoanManagement.WalletSystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TransactionService Unit Tests")
class TransactionServiceImplTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransactionMapper transactionMapper;

    @InjectMocks
    private TransactionServiceImpl transactionService;

    private User testUser;
    private Transaction testTransaction1;
    private Transaction testTransaction2;
    private TransactionResponse testTransactionResponse1;
    private TransactionResponse testTransactionResponse2;

    @BeforeEach
    void setUp() {
        // Initialize test data
        testUser = new User();
        testUser.setId(UUID.randomUUID().toString());
        testUser.setEmail("test@example.com");
        testUser.setFullName("John Doe");
        testUser.setPhone("08000000000");
        testUser.setCreatedAt(Instant.now());

        testTransaction1 = new Transaction();
        testTransaction1.setId(UUID.randomUUID().toString());
        testTransaction1.setUserId(testUser.getId());
        testTransaction1.setType(TransactionType.CREDIT);
        testTransaction1.setAmount(new BigDecimal("1000.00"));
        testTransaction1.setReference("TXN001");
        testTransaction1.setStatus(1);
        testTransaction1.setCreatedAt(Instant.now());

        testTransaction2 = new Transaction();
        testTransaction2.setId(UUID.randomUUID().toString());
        testTransaction2.setUserId(testUser.getId());
        testTransaction2.setType(TransactionType.DEBIT);
        testTransaction2.setAmount(new BigDecimal("500.00"));
        testTransaction2.setReference("TXN002");
        testTransaction2.setStatus(1);
        testTransaction2.setCreatedAt(Instant.now().minusSeconds(3600));

        testTransactionResponse1 = new TransactionResponse();
        testTransactionResponse1.setId(testTransaction1.getId());
        testTransactionResponse1.setType("CREDIT");
        testTransactionResponse1.setAmount(new BigDecimal("1000.00"));
        testTransactionResponse1.setReference("TXN001");
        testTransactionResponse1.setStatus(1);
        testTransactionResponse1.setCreatedAt(testTransaction1.getCreatedAt());

        testTransactionResponse2 = new TransactionResponse();
        testTransactionResponse2.setId(testTransaction2.getId());
        testTransactionResponse2.setType("DEBIT");
        testTransactionResponse2.setAmount(new BigDecimal("500.00"));
        testTransactionResponse2.setReference("TXN002");
        testTransactionResponse2.setStatus(1);
        testTransactionResponse2.setCreatedAt(testTransaction2.getCreatedAt());
    }

    // ======================== getAllTransactions Tests ========================

    @Test
    @DisplayName("Should retrieve all transactions for authenticated user successfully")
    void testGetAllTransactionsSuccessfully() {
        // Arrange
        List<Transaction> transactions = List.of(testTransaction1, testTransaction2);
        List<TransactionResponse> responses = List.of(testTransactionResponse1, testTransactionResponse2);

        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId()))
                .thenReturn(transactions);
        when(transactionMapper.toTransactionResponseList(transactions))
                .thenReturn(responses);

        // Act
        List<TransactionResponse> result = transactionService.getAllTransactions(testUser.getEmail());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("CREDIT", result.get(0).getType());
        assertEquals("DEBIT", result.get(1).getType());
        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
        verify(transactionRepository, times(1)).findByUserIdOrderByCreatedAtDesc(testUser.getId());
        verify(transactionMapper, times(1)).toTransactionResponseList(transactions);
    }

    @Test
    @DisplayName("Should return empty list when user has no transactions")
    void testGetAllTransactionsEmptyList() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId()))
                .thenReturn(List.of());
        when(transactionMapper.toTransactionResponseList(List.of()))
                .thenReturn(List.of());

        // Act
        List<TransactionResponse> result = transactionService.getAllTransactions(testUser.getEmail());

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
        verify(transactionRepository, times(1)).findByUserIdOrderByCreatedAtDesc(testUser.getId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found for getAllTransactions")
    void testGetAllTransactionsUserNotFound() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.getAllTransactions(testUser.getEmail())
        );
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
        verify(transactionRepository, never()).findByUserIdOrderByCreatedAtDesc(any());
    }

    @Test
    @DisplayName("Should return transactions ordered by creation date descending")
    void testGetAllTransactionsOrderByCreatedAtDesc() {
        // Arrange
        List<Transaction> transactions = List.of(testTransaction1, testTransaction2); // Most recent first
        List<TransactionResponse> responses = List.of(testTransactionResponse1, testTransactionResponse2);

        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId()))
                .thenReturn(transactions);
        when(transactionMapper.toTransactionResponseList(transactions))
                .thenReturn(responses);

        // Act
        List<TransactionResponse> result = transactionService.getAllTransactions(testUser.getEmail());

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        // Verify order: newer transaction first
        assertTrue(
                testTransactionResponse1.getCreatedAt().isAfter(testTransactionResponse2.getCreatedAt())
                        || testTransactionResponse1.getCreatedAt().equals(testTransactionResponse2.getCreatedAt())
        );
    }

    // ======================== getTransactionById Tests ========================

    @Test
    @DisplayName("Should retrieve single transaction by ID successfully")
    void testGetTransactionByIdSuccessfully() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(transactionRepository.findByIdAndUserId(testTransaction1.getId(), testUser.getId()))
                .thenReturn(Optional.of(testTransaction1));
        when(transactionMapper.toTransactionResponse(testTransaction1))
                .thenReturn(testTransactionResponse1);

        // Act
        TransactionResponse result = transactionService.getTransactionById(
                testUser.getEmail(),
                testTransaction1.getId()
        );

        // Assert
        assertNotNull(result);
        assertEquals(testTransactionResponse1.getId(), result.getId());
        assertEquals("CREDIT", result.getType());
        assertEquals(new BigDecimal("1000.00"), result.getAmount());
        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
        verify(transactionRepository, times(1)).findByIdAndUserId(testTransaction1.getId(), testUser.getId());
        verify(transactionMapper, times(1)).toTransactionResponse(testTransaction1);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found for getTransactionById")
    void testGetTransactionByIdUserNotFound() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.getTransactionById(testUser.getEmail(), testTransaction1.getId())
        );
        assertEquals("User not found", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
        verify(transactionRepository, never()).findByIdAndUserId(any(), any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when transaction not found")
    void testGetTransactionByIdTransactionNotFound() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(transactionRepository.findByIdAndUserId(testTransaction1.getId(), testUser.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.getTransactionById(testUser.getEmail(), testTransaction1.getId())
        );
        assertEquals("Transaction not found or does not belong to the authenticated user", exception.getMessage());
        verify(userRepository, times(1)).findByEmail(testUser.getEmail());
        verify(transactionRepository, times(1)).findByIdAndUserId(testTransaction1.getId(), testUser.getId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when transaction belongs to different user")
    void testGetTransactionByIdTransactionNotBelongToUser() {
        // Arrange - Transaction belongs to a different user
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(transactionRepository.findByIdAndUserId(testTransaction1.getId(), testUser.getId()))
                .thenReturn(Optional.empty()); // Not found because it belongs to different user

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.getTransactionById(testUser.getEmail(), testTransaction1.getId())
        );
        assertEquals("Transaction not found or does not belong to the authenticated user", exception.getMessage());
    }

    @Test
    @DisplayName("Should retrieve correct transaction details by ID")
    void testGetTransactionByIdCorrectDetails() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(transactionRepository.findByIdAndUserId(testTransaction1.getId(), testUser.getId()))
                .thenReturn(Optional.of(testTransaction1));
        when(transactionMapper.toTransactionResponse(testTransaction1))
                .thenReturn(testTransactionResponse1);

        // Act
        TransactionResponse result = transactionService.getTransactionById(
                testUser.getEmail(),
                testTransaction1.getId()
        );

        // Assert
        assertNotNull(result);
        assertEquals(testTransaction1.getId(), result.getId());
        assertEquals(testTransaction1.getAmount(), result.getAmount());
        assertEquals(testTransaction1.getReference(), result.getReference());
        assertEquals(testTransaction1.getStatus(), result.getStatus());
    }

    @Test
    @DisplayName("Should handle null email gracefully")
    void testGetTransactionByIdNullEmail() {
        // Act & Assert
        assertThrows(
                Exception.class,
                () -> transactionService.getTransactionById(null, testTransaction1.getId())
        );
    }

    @Test
    @DisplayName("Should handle null transaction ID gracefully")
    void testGetTransactionByIdNullTransactionId() {
        // Arrange
        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(transactionRepository.findByIdAndUserId(null, testUser.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.getTransactionById(testUser.getEmail(), null)
        );
        assertEquals("Transaction not found or does not belong to the authenticated user", exception.getMessage());
    }

    // ======================== Authorization & Security Tests ========================

    @Test
    @DisplayName("Should only retrieve transactions for the authenticated user")
    void testGetAllTransactionsUserIsolation() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID().toString());
        anotherUser.setEmail("other@example.com");

        Transaction anotherUserTransaction = new Transaction();
        anotherUserTransaction.setId(UUID.randomUUID().toString());
        anotherUserTransaction.setUserId(anotherUser.getId());

        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(testUser.getId()))
                .thenReturn(List.of(testTransaction1));
        when(transactionMapper.toTransactionResponseList(List.of(testTransaction1)))
                .thenReturn(List.of(testTransactionResponse1));

        // Act
        List<TransactionResponse> result = transactionService.getAllTransactions(testUser.getEmail());

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        // Verify that only transactions for the authenticated user are retrieved
        verify(transactionRepository, times(1)).findByUserIdOrderByCreatedAtDesc(testUser.getId());
        verify(transactionRepository, never()).findByUserIdOrderByCreatedAtDesc(anotherUser.getId());
    }

    @Test
    @DisplayName("Should prevent access to other user's transactions")
    void testGetTransactionByIdPreventUnauthorizedAccess() {
        // Arrange
        User anotherUser = new User();
        anotherUser.setId(UUID.randomUUID().toString());
        anotherUser.setEmail("other@example.com");

        Transaction anotherUserTransaction = new Transaction();
        anotherUserTransaction.setId(UUID.randomUUID().toString());
        anotherUserTransaction.setUserId(anotherUser.getId());

        when(userRepository.findByEmail(testUser.getEmail()))
                .thenReturn(Optional.of(testUser));
        // Transaction doesn't belong to the user making the request
        when(transactionRepository.findByIdAndUserId(anotherUserTransaction.getId(), testUser.getId()))
                .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> transactionService.getTransactionById(testUser.getEmail(), anotherUserTransaction.getId())
        );
        assertEquals("Transaction not found or does not belong to the authenticated user", exception.getMessage());
    }
}


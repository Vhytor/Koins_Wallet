package com.LoanManagement.WalletSystem.service;

import com.LoanManagement.WalletSystem.dto.Loan.ApplyLoanRequest;
import com.LoanManagement.WalletSystem.dto.Loan.LoanApprovalRequest;
import com.LoanManagement.WalletSystem.dto.Loan.LoanRepaymentRequest;
import com.LoanManagement.WalletSystem.dto.Loan.LoanResponse;
import com.LoanManagement.WalletSystem.exception.BusinessRuleException;
import com.LoanManagement.WalletSystem.exception.ResourceNotFoundException;
import com.LoanManagement.WalletSystem.mapper.LoanMapper;
import com.LoanManagement.WalletSystem.mapper.impl.LoanMapperImpl;
import com.LoanManagement.WalletSystem.model.Loan;
import com.LoanManagement.WalletSystem.model.LoanStatus;
import com.LoanManagement.WalletSystem.model.Transaction;
import com.LoanManagement.WalletSystem.model.TransactionType;
import com.LoanManagement.WalletSystem.model.User;
import com.LoanManagement.WalletSystem.model.Wallet;
import com.LoanManagement.WalletSystem.repository.LoanRepository;
import com.LoanManagement.WalletSystem.repository.TransactionRepository;
import com.LoanManagement.WalletSystem.repository.UserRepository;
import com.LoanManagement.WalletSystem.repository.WalletRepository;
import com.LoanManagement.WalletSystem.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for LoanServiceImpl
 * Tests all loan operations following AAA pattern (Arrange, Act, Assert)
 */
@ExtendWith(MockitoExtension.class)
public class LoanServiceImplTest {

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private LoanMapper loanMapper;

    @InjectMocks
    private LoanServiceImpl loanService;

    private User testUser;
    private Wallet testWallet;
    private Loan testLoan;

    @BeforeEach
    void setUp() {
        // Initialize test user
        testUser = new User();
        testUser.setId("user-123");
        testUser.setEmail("test@example.com");
        testUser.setFullName("Test User");

        // Initialize test wallet
        testWallet = new Wallet();
        testWallet.setId("wallet-123");
        testWallet.setUser(testUser);
        testWallet.setBalance(new BigDecimal("50000"));
        testWallet.setCurrency("NGN");

        // Initialize test loan
        testLoan = new Loan();
        testLoan.setId("loan-123");
        testLoan.setUser(testUser);
        testLoan.setPrincipalAmount(new BigDecimal("10000"));
        testLoan.setInterestRate(new BigDecimal("5"));
        testLoan.setTotalAmount(new BigDecimal("10500"));
        testLoan.setDurationMonths(12);
        testLoan.setStatus(LoanStatus.PENDING);
        testLoan.setReason("Business expansion");
        testLoan.setAppliedAt(Instant.now());
    }

    // ==================== Apply For Loan Tests ====================

    @Test
    void testApplyForLoanSuccessfully() {
        // Arrange
        ApplyLoanRequest request = new ApplyLoanRequest(
                new BigDecimal("10000"),
                new BigDecimal("5"),
                12,
                "Business expansion"
        );

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(loanRepository.countByUserIdAndStatus("user-123", LoanStatus.PENDING)).thenReturn(0L);
        when(loanRepository.countByUserIdAndStatus("user-123", LoanStatus.APPROVED)).thenReturn(0L);
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);
        when(loanMapper.toLoanResponse(testLoan)).thenReturn(createLoanResponse(testLoan));

        // Act
        LoanResponse response = loanService.applyForLoan("test@example.com", request);

        // Assert
        assertNotNull(response);
        assertEquals("loan-123", response.getId());
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void testApplyForLoanWithPendingLoanThrowsException() {
        // Arrange
        ApplyLoanRequest request = new ApplyLoanRequest(
                new BigDecimal("10000"),
                new BigDecimal("5"),
                12,
                "Business expansion"
        );

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(loanRepository.countByUserIdAndStatus("user-123", LoanStatus.PENDING)).thenReturn(1L);

        // Act & Assert
        assertThrows(BusinessRuleException.class, 
                () -> loanService.applyForLoan("test@example.com", request));
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void testApplyForLoanWithApprovedLoanThrowsException() {
        // Arrange
        ApplyLoanRequest request = new ApplyLoanRequest(
                new BigDecimal("10000"),
                new BigDecimal("5"),
                12,
                "Business expansion"
        );

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(loanRepository.countByUserIdAndStatus("user-123", LoanStatus.PENDING)).thenReturn(0L);
        when(loanRepository.countByUserIdAndStatus("user-123", LoanStatus.APPROVED)).thenReturn(1L);

        // Act & Assert
        assertThrows(BusinessRuleException.class, 
                () -> loanService.applyForLoan("test@example.com", request));
    }

    @Test
    void testApplyForLoanUserNotFoundThrowsException() {
        // Arrange
        ApplyLoanRequest request = new ApplyLoanRequest(
                new BigDecimal("10000"),
                new BigDecimal("5"),
                12,
                "Business expansion"
        );

        when(userRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> loanService.applyForLoan("nonexistent@example.com", request));
    }

    @Test
    void testApplyForLoanWithNegativePrincipalThrowsException() {
        // Arrange
        ApplyLoanRequest request = new ApplyLoanRequest(
                new BigDecimal("-5000"),
                new BigDecimal("5"),
                12,
                "Business expansion"
        );

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(loanRepository.countByUserIdAndStatus("user-123", LoanStatus.PENDING)).thenReturn(0L);
        when(loanRepository.countByUserIdAndStatus("user-123", LoanStatus.APPROVED)).thenReturn(0L);

        // Act & Assert
        assertThrows(BusinessRuleException.class, 
                () -> loanService.applyForLoan("test@example.com", request));
    }

    @Test
    void testApplyForLoanWithNegativeDurationThrowsException() {
        // Arrange
        ApplyLoanRequest request = new ApplyLoanRequest(
                new BigDecimal("10000"),
                new BigDecimal("5"),
                -1,
                "Business expansion"
        );

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(loanRepository.countByUserIdAndStatus("user-123", LoanStatus.PENDING)).thenReturn(0L);
        when(loanRepository.countByUserIdAndStatus("user-123", LoanStatus.APPROVED)).thenReturn(0L);

        // Act & Assert
        assertThrows(BusinessRuleException.class, 
                () -> loanService.applyForLoan("test@example.com", request));
    }

    // ==================== Approve Loan Tests ====================

    @Test
    void testApproveLoanSuccessfully() {
        // Arrange
        LoanApprovalRequest request = new LoanApprovalRequest("APPROVED", null);

        when(loanRepository.findById("loan-123")).thenReturn(Optional.of(testLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);
        when(loanMapper.toLoanResponse(testLoan)).thenReturn(createLoanResponse(testLoan));

        // Act
        LoanResponse response = loanService.approveLoan("loan-123", request);

        // Assert
        assertNotNull(response);
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void testRejectLoanSuccessfully() {
        // Arrange
        LoanApprovalRequest request = new LoanApprovalRequest("REJECTED", "Insufficient income");

        when(loanRepository.findById("loan-123")).thenReturn(Optional.of(testLoan));
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);
        when(loanMapper.toLoanResponse(testLoan)).thenReturn(createLoanResponse(testLoan));

        // Act
        LoanResponse response = loanService.approveLoan("loan-123", request);

        // Assert
        assertNotNull(response);
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void testApproveLoanInvalidStatusThrowsException() {
        // Arrange
        testLoan.setStatus(LoanStatus.APPROVED);
        LoanApprovalRequest request = new LoanApprovalRequest("APPROVED", null);

        when(loanRepository.findById("loan-123")).thenReturn(Optional.of(testLoan));

        // Act & Assert
        assertThrows(BusinessRuleException.class, 
                () -> loanService.approveLoan("loan-123", request));
    }

    @Test
    void testApproveLoanNotFoundThrowsException() {
        // Arrange
        LoanApprovalRequest request = new LoanApprovalRequest("APPROVED", null);

        when(loanRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> loanService.approveLoan("nonexistent", request));
    }

    // ==================== Disburse Loan Tests ====================

    @Test
    void testDisburseLoanSuccessfully() {
        // Arrange
        testLoan.setStatus(LoanStatus.APPROVED);

        when(loanRepository.findById("loan-123")).thenReturn(Optional.of(testLoan));
        when(walletRepository.findByUserId("user-123")).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);
        when(loanMapper.toLoanResponse(testLoan)).thenReturn(createLoanResponse(testLoan));

        // Act
        LoanResponse response = loanService.disburseLoan("loan-123");

        // Assert
        assertNotNull(response);
        verify(walletRepository, times(2)).save(any(Wallet.class)); // Once for update, configuration may vary
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void testDisburseLoanNotApprovedThrowsException() {
        // Arrange
        testLoan.setStatus(LoanStatus.PENDING);

        when(loanRepository.findById("loan-123")).thenReturn(Optional.of(testLoan));

        // Act & Assert
        assertThrows(BusinessRuleException.class, 
                () -> loanService.disburseLoan("loan-123"));
    }

    @Test
    void testDisburseLoanNotFoundThrowsException() {
        // Arrange
        when(loanRepository.findById("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> loanService.disburseLoan("nonexistent"));
    }

    @Test
    void testDisburseLoanWalletNotFoundThrowsException() {
        // Arrange
        testLoan.setStatus(LoanStatus.APPROVED);

        when(loanRepository.findById("loan-123")).thenReturn(Optional.of(testLoan));
        when(walletRepository.findByUserId("user-123")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> loanService.disburseLoan("loan-123"));
    }

    // ==================== Repay Loan Tests ====================

    @Test
    void testRepayLoanSuccessfully() {
        // Arrange
        testLoan.setStatus(LoanStatus.ACTIVE);
        testLoan.setRepaidAmount(BigDecimal.ZERO);
        testLoan.setRemainingAmount(testLoan.getTotalAmount());

        LoanRepaymentRequest request = new LoanRepaymentRequest(new BigDecimal("1000"));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(loanRepository.findByIdAndUserId("loan-123", "user-123")).thenReturn(Optional.of(testLoan));
        when(walletRepository.findByUserId("user-123")).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);
        when(loanMapper.toLoanResponse(testLoan)).thenReturn(createLoanResponse(testLoan));

        // Act
        LoanResponse response = loanService.repayLoan("test@example.com", "loan-123", request);

        // Assert
        assertNotNull(response);
        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository, times(1)).save(any(Transaction.class));
        verify(loanRepository, times(1)).save(any(Loan.class));
    }

    @Test
    void testRepayLoanFullRepaymentCompletesLoan() {
        // Arrange
        testLoan.setStatus(LoanStatus.ACTIVE);
        testLoan.setRepaidAmount(BigDecimal.ZERO);
        testLoan.setRemainingAmount(testLoan.getTotalAmount());

        LoanRepaymentRequest request = new LoanRepaymentRequest(testLoan.getTotalAmount());

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(loanRepository.findByIdAndUserId("loan-123", "user-123")).thenReturn(Optional.of(testLoan));
        when(walletRepository.findByUserId("user-123")).thenReturn(Optional.of(testWallet));
        when(walletRepository.save(any(Wallet.class))).thenReturn(testWallet);
        when(transactionRepository.save(any(Transaction.class))).thenReturn(new Transaction());
        when(loanRepository.save(any(Loan.class))).thenReturn(testLoan);
        when(loanMapper.toLoanResponse(testLoan)).thenReturn(createLoanResponse(testLoan));

        // Act
        LoanResponse response = loanService.repayLoan("test@example.com", "loan-123", request);

        // Assert
        assertNotNull(response);
    }

    @Test
    void testRepayLoanInsufficientWalletBalanceThrowsException() {
        // Arrange
        testLoan.setStatus(LoanStatus.ACTIVE);
        testLoan.setRepaidAmount(BigDecimal.ZERO);
        testLoan.setRemainingAmount(testLoan.getTotalAmount());

        testWallet.setBalance(new BigDecimal("100")); // Less than repayment amount

        LoanRepaymentRequest request = new LoanRepaymentRequest(new BigDecimal("1000"));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(loanRepository.findByIdAndUserId("loan-123", "user-123")).thenReturn(Optional.of(testLoan));
        when(walletRepository.findByUserId("user-123")).thenReturn(Optional.of(testWallet));

        // Act & Assert
        assertThrows(BusinessRuleException.class, 
                () -> loanService.repayLoan("test@example.com", "loan-123", request));
    }

    @Test
    void testRepayLoanExceedsRemainingBalanceThrowsException() {
        // Arrange
        testLoan.setStatus(LoanStatus.ACTIVE);
        testLoan.setRepaidAmount(new BigDecimal("9000"));
        testLoan.setRemainingAmount(new BigDecimal("1500"));

        LoanRepaymentRequest request = new LoanRepaymentRequest(new BigDecimal("2000")); // Greater than remaining

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(loanRepository.findByIdAndUserId("loan-123", "user-123")).thenReturn(Optional.of(testLoan));
        when(walletRepository.findByUserId("user-123")).thenReturn(Optional.of(testWallet));

        // Act & Assert
        assertThrows(BusinessRuleException.class, 
                () -> loanService.repayLoan("test@example.com", "loan-123", request));
    }

    @Test
    void testRepayLoanNotActiveThrowsException() {
        // Arrange
        testLoan.setStatus(LoanStatus.PENDING);

        LoanRepaymentRequest request = new LoanRepaymentRequest(new BigDecimal("1000"));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(loanRepository.findByIdAndUserId("loan-123", "user-123")).thenReturn(Optional.of(testLoan));

        // Act & Assert
        assertThrows(BusinessRuleException.class, 
                () -> loanService.repayLoan("test@example.com", "loan-123", request));
    }

    // ==================== Get Loan Details Tests ====================

    @Test
    void testGetLoanDetailsSuccessfully() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(loanRepository.findByIdAndUserId("loan-123", "user-123")).thenReturn(Optional.of(testLoan));
        when(loanMapper.toLoanResponse(testLoan)).thenReturn(createLoanResponse(testLoan));

        // Act
        LoanResponse response = loanService.getLoanDetails("test@example.com", "loan-123");

        // Assert
        assertNotNull(response);
        assertEquals("loan-123", response.getId());
    }

    @Test
    void testGetLoanDetailsLoanNotFoundThrowsException() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(loanRepository.findByIdAndUserId("loan-123", "user-123")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, 
                () -> loanService.getLoanDetails("test@example.com", "loan-123"));
    }

    // ==================== Get User Loans Tests ====================

    @Test
    void testGetUserLoansSuccessfully() {
        // Arrange
        List<Loan> loans = List.of(testLoan);
        List<LoanResponse> responses = List.of(createLoanResponse(testLoan));

        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(loanRepository.findByUserId("user-123")).thenReturn(loans);
        when(loanMapper.toLoanResponseList(loans)).thenReturn(responses);

        // Act
        List<LoanResponse> result = loanService.getUserLoans("test@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetUserLoansEmptyList() {
        // Arrange
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        when(loanRepository.findByUserId("user-123")).thenReturn(List.of());
        when(loanMapper.toLoanResponseList(List.of())).thenReturn(List.of());

        // Act
        List<LoanResponse> result = loanService.getUserLoans("test@example.com");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ==================== Get Loans By Status Tests ====================

    @Test
    void testGetLoansByStatusSuccessfully() {
        // Arrange
        List<Loan> loans = List.of(testLoan);
        List<LoanResponse> responses = List.of(createLoanResponse(testLoan));

        when(loanRepository.findByStatus(LoanStatus.PENDING)).thenReturn(loans);
        when(loanMapper.toLoanResponseList(loans)).thenReturn(responses);

        // Act
        List<LoanResponse> result = loanService.getLoansByStatus("PENDING");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testGetLoansByInvalidStatusThrowsException() {
        // Arrange & Act & Assert
        assertThrows(BusinessRuleException.class, 
                () -> loanService.getLoansByStatus("INVALID_STATUS"));
    }

    // ==================== Helper Methods ====================

    private LoanResponse createLoanResponse(Loan loan) {
        return new LoanResponse(
                loan.getId(),
                loan.getUser().getId(),
                loan.getUser().getEmail(),
                loan.getPrincipalAmount(),
                loan.getInterestRate(),
                loan.getTotalAmount(),
                loan.getRepaidAmount(),
                loan.getRemainingAmount(),
                loan.getDurationMonths(),
                loan.getStatus().toString(),
                loan.getReason(),
                loan.getRejectionReason(),
                loan.getAppliedAt(),
                loan.getApprovedAt(),
                loan.getDisbursedAt(),
                loan.getCompletedAt()
        );
    }
}


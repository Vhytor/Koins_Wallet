package com.LoanManagement.WalletSystem.service.impl;

import com.LoanManagement.WalletSystem.dto.Loan.ApplyLoanRequest;
import com.LoanManagement.WalletSystem.dto.Loan.LoanApprovalRequest;
import com.LoanManagement.WalletSystem.dto.Loan.LoanRepaymentRequest;
import com.LoanManagement.WalletSystem.dto.Loan.LoanResponse;
import com.LoanManagement.WalletSystem.exception.BusinessRuleException;
import com.LoanManagement.WalletSystem.exception.ResourceNotFoundException;
import com.LoanManagement.WalletSystem.mapper.LoanMapper;
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
import com.LoanManagement.WalletSystem.service.LoanService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

/**
 * Implementation of LoanService
 * Handles all loan-related business logic following Service layer pattern
 * Uses transactions to ensure data consistency
 */
@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final LoanMapper loanMapper;

    public LoanServiceImpl(
            LoanRepository loanRepository,
            UserRepository userRepository,
            WalletRepository walletRepository,
            TransactionRepository transactionRepository,
            LoanMapper loanMapper
    ) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.loanMapper = loanMapper;
    }

    @Override
    @Transactional
    public LoanResponse applyForLoan(String userEmail, ApplyLoanRequest request) {
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if user already has a pending or approved loan
        long pendingLoans = loanRepository.countByUserIdAndStatus(user.getId(), LoanStatus.PENDING);
        if (pendingLoans > 0) {
            throw new BusinessRuleException("User already has a pending loan application. Please wait for approval or rejection.");
        }

        long approvedLoans = loanRepository.countByUserIdAndStatus(user.getId(), LoanStatus.APPROVED);
        if (approvedLoans > 0) {
            throw new BusinessRuleException("User already has an approved loan awaiting disbursement.");
        }

        // Validate request parameters
        if (request.getPrincipalAmount() == null || request.getPrincipalAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Principal amount must be greater than zero");
        }

        if (request.getInterestRate() == null || request.getInterestRate().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Interest rate cannot be negative");
        }

        if (request.getDurationMonths() == null || request.getDurationMonths() <= 0) {
            throw new BusinessRuleException("Duration must be greater than zero months");
        }

        // Create loan application
        Loan loan = new Loan(
                user,
                request.getPrincipalAmount(),
                request.getInterestRate(),
                request.getDurationMonths(),
                request.getReason()
        );

        Loan savedLoan = loanRepository.save(loan);
        return loanMapper.toLoanResponse(savedLoan);
    }

    @Override
    @Transactional
    public LoanResponse approveLoan(String loanId, LoanApprovalRequest request) {
        // Get loan
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        // Check loan status
        if (!loan.getStatus().equals(LoanStatus.PENDING)) {
            throw new BusinessRuleException("Only pending loans can be approved or rejected");
        }

        // Validate decision
        String decision = request.getDecision().toUpperCase();
        if (!decision.equals("APPROVED") && !decision.equals("REJECTED")) {
            throw new BusinessRuleException("Decision must be either APPROVED or REJECTED");
        }

        if (decision.equals("APPROVED")) {
            loan.setStatus(LoanStatus.APPROVED);
            loan.setApprovedAt(Instant.now());
        } else {
            loan.setStatus(LoanStatus.REJECTED);
            loan.setRejectionReason(request.getRejectionReason());
            loan.setApprovedAt(Instant.now()); // Mark when decision was made
        }

        Loan savedLoan = loanRepository.save(loan);
        return loanMapper.toLoanResponse(savedLoan);
    }

    @Override
    @Transactional
    public LoanResponse disburseLoan(String loanId) {
        // Get loan
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found"));

        // Check loan status
        if (!loan.getStatus().equals(LoanStatus.APPROVED)) {
            throw new BusinessRuleException("Only approved loans can be disbursed");
        }

        // Get user's wallet
        User user = loan.getUser();
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User wallet not found"));

        // Disburse loan to wallet
        BigDecimal newBalance = wallet.getBalance().add(loan.getTotalAmount());
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        // Create disbursement transaction
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setUserId(user.getId());
        transaction.setType(TransactionType.CREDIT); // Loan disbursement is a credit
        transaction.setAmount(loan.getTotalAmount());
        transaction.setReference("Loan Disbursement - " + loan.getId());
        transaction.setStatus(1); // Success
        transactionRepository.save(transaction);

        // Update loan status
        loan.setStatus(LoanStatus.ACTIVE);
        loan.setDisbursedAt(Instant.now());

        Loan savedLoan = loanRepository.save(loan);
        return loanMapper.toLoanResponse(savedLoan);
    }

    @Override
    @Transactional
    public LoanResponse repayLoan(String userEmail, String loanId, LoanRepaymentRequest request) {
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get loan
        Loan loan = loanRepository.findByIdAndUserId(loanId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found or does not belong to user"));

        // Check loan status - can only repay ACTIVE loans
        if (!loan.getStatus().equals(LoanStatus.ACTIVE)) {
            throw new BusinessRuleException("Loan must be active to make repayment");
        }

        // Validate repayment amount
        if (request.getRepaymentAmount() == null || request.getRepaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Repayment amount must be greater than zero");
        }

        // Check if repayment exceeds remaining balance
        if (request.getRepaymentAmount().compareTo(loan.getRemainingAmount()) > 0) {
            throw new BusinessRuleException("Repayment amount cannot exceed remaining balance of " + loan.getRemainingAmount());
        }

        // Get user's wallet for debit
        Wallet wallet = walletRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User wallet not found"));

        // Check wallet balance
        if (wallet.getBalance().compareTo(request.getRepaymentAmount()) < 0) {
            throw new BusinessRuleException("Insufficient wallet balance for repayment");
        }

        // Deduct from wallet
        BigDecimal newBalance = wallet.getBalance().subtract(request.getRepaymentAmount());
        wallet.setBalance(newBalance);
        walletRepository.save(wallet);

        // Create repayment transaction
        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setUserId(user.getId());
        transaction.setType(TransactionType.DEBIT);
        transaction.setAmount(request.getRepaymentAmount());
        transaction.setReference("Loan Repayment - " + loan.getId());
        transaction.setStatus(1); // Success
        transactionRepository.save(transaction);

        // Update loan repayment details
        BigDecimal newRepaidAmount = loan.getRepaidAmount().add(request.getRepaymentAmount());
        BigDecimal newRemainingAmount = loan.getTotalAmount().subtract(newRepaidAmount);

        loan.setRepaidAmount(newRepaidAmount);
        loan.setRemainingAmount(newRemainingAmount);

        // Check if loan is fully repaid
        if (newRemainingAmount.compareTo(BigDecimal.ZERO) <= 0) {
            loan.setStatus(LoanStatus.COMPLETED);
            loan.setCompletedAt(Instant.now());
        }

        Loan savedLoan = loanRepository.save(loan);
        return loanMapper.toLoanResponse(savedLoan);
    }

    @Override
    public LoanResponse getLoanDetails(String userEmail, String loanId) {
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Get loan and verify ownership
        Loan loan = loanRepository.findByIdAndUserId(loanId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found or does not belong to user"));

        return loanMapper.toLoanResponse(loan);
    }

    @Override
    public List<LoanResponse> getUserLoans(String userEmail) {
        // Get user
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        List<Loan> loans = loanRepository.findByUserId(user.getId());
        return loanMapper.toLoanResponseList(loans);
    }

    @Override
    public List<LoanResponse> getLoansByStatus(String status) {
        // Parse status enum
        LoanStatus loanStatus;
        try {
            loanStatus = LoanStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleException("Invalid loan status. Valid values are: PENDING, APPROVED, REJECTED, DISBURSED, ACTIVE, COMPLETED, DEFAULTED");
        }

        List<Loan> loans = loanRepository.findByStatus(loanStatus);
        return loanMapper.toLoanResponseList(loans);
    }

    @Override
    public List<LoanResponse> getAllLoans() {
        List<Loan> loans = loanRepository.findAll();
        return loanMapper.toLoanResponseList(loans);
    }
}


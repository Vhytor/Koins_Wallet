package com.LoanManagement.WalletSystem.service.impl;

import com.LoanManagement.WalletSystem.dto.Auth.*;
import com.LoanManagement.WalletSystem.exception.AuthenticationFailedException;
import com.LoanManagement.WalletSystem.exception.BusinessRuleException;
import com.LoanManagement.WalletSystem.exception.ResourceNotFoundException;
import com.LoanManagement.WalletSystem.mapper.UserMapper;
import com.LoanManagement.WalletSystem.model.Role;
import com.LoanManagement.WalletSystem.model.User;
import com.LoanManagement.WalletSystem.model.Wallet;
import com.LoanManagement.WalletSystem.repository.UserRepository;
import com.LoanManagement.WalletSystem.repository.WalletRepository;
import com.LoanManagement.WalletSystem.security.JwtUtil;
import com.LoanManagement.WalletSystem.service.AuthService;
import com.LoanManagement.WalletSystem.service.EmailService;
import com.LoanManagement.WalletSystem.service.OtpService;
import com.LoanManagement.WalletSystem.service.TokenBlacklistService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;


@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final OtpService otpService;
    private final EmailService emailService;
    private final TokenBlacklistService tokenBlacklistService;

    public AuthServiceImpl(
            UserRepository userRepository,
            WalletRepository walletRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserMapper userMapper,
            OtpService otpService,
            EmailService emailService,
            TokenBlacklistService tokenBlacklistService
    ) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
        this.otpService = otpService;
        this.emailService = emailService;
        this.tokenBlacklistService = tokenBlacklistService;
    }

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Validation
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email already in use");
        }
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessRuleException("Phone number already in use");
        }

        // Create user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setBvn(request.getBvn());
        user.setRoles(Collections.singleton(Role.ROLE_USER));

        User savedUser = userRepository.save(user);

        // Auto-create wallet for the user
        Wallet wallet = new Wallet();
        wallet.setUser(savedUser);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setCurrency("NGN");
        walletRepository.save(wallet);

        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate the user credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // Generate JWT token
            String token = jwtUtil.generateToken(request.getEmail());

            AuthResponse response = new AuthResponse(token);
            response.setTokenType("Bearer");
            return response;
        } catch (BadCredentialsException ex) {
            throw new AuthenticationFailedException("Invalid email or password");
        } catch (Exception ex) {
            throw new AuthenticationFailedException("Authentication failed: " + ex.getMessage());
        }
    }

    @Override
    @Transactional
    public void logout(String token) {
        // Extract email from token
        String userEmail = jwtUtil.getSubject(token);

        // Get token expiration time
        long expiresAtSeconds = jwtUtil.getExpirationDate(token).getTime() / 1000;

        // Blacklist the token
        tokenBlacklistService.blacklistToken(token, userEmail, expiresAtSeconds);
    }

    @Override
    @Transactional
    public OtpResponse recoverPassword(PasswordRecoveryRequest request) {
        // Verify user exists
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this email"));

        // Generate OTP
        String otp = otpService.generateOtp(request.getEmail());

        // Send OTP to email
        emailService.sendOtpEmail(request.getEmail(), otp);

        // Get remaining time for the OTP
        Long remainingTime = otpService.getRemainingOtpTime(request.getEmail());

        return new OtpResponse(
                "OTP sent successfully to your email",
                request.getEmail(),
                remainingTime
        );
    }

    @Override
    @Transactional
    public OtpResponse resendOtp(PasswordRecoveryRequest request) {
        // Verify user exists
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with this email"));

        // Generate new OTP
        String otp = otpService.generateOtp(request.getEmail());

        // Send OTP to email
        emailService.sendOtpEmail(request.getEmail(), otp);

        // Get remaining time for the OTP
        Long remainingTime = otpService.getRemainingOtpTime(request.getEmail());

        return new OtpResponse(
                "OTP resent successfully to your email",
                request.getEmail(),
                remainingTime
        );
    }

    @Override
    @Transactional
    public AuthResponse resetPassword(PasswordResetRequest request) {
        // Verify user exists
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Verify OTP
        if (!otpService.verifyOtp(request.getEmail(), request.getOtp())) {
            throw new BusinessRuleException("Invalid or expired OTP");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // Mark OTP as used
        otpService.markOtpAsUsed(request.getEmail(), request.getOtp());

        // Send confirmation email
        emailService.sendPasswordResetConfirmationEmail(request.getEmail());

        // Generate new login token
        String token = jwtUtil.generateToken(request.getEmail());
        AuthResponse response = new AuthResponse(token);
        response.setTokenType("Bearer");
        return response;
    }

    @Override
    @Transactional
    public UserResponse updateProfile(String userEmail, UpdateProfileRequest request) {
        // Verify user exists
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Check if new phone is unique (if being changed)
        if (request.getPhone() != null && !request.getPhone().equals(user.getPhone())) {
            if (userRepository.existsByPhone(request.getPhone())) {
                throw new BusinessRuleException("Phone number is already in use");
            }
        }

        // Update fields
        user.setFullName(request.getFullName());
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getBvn() != null) {
            user.setBvn(request.getBvn());
        }

        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }
}



package com.LoanManagement.WalletSystem.controller;

import com.LoanManagement.WalletSystem.dto.Auth.*;
import com.LoanManagement.WalletSystem.service.AuthService;
import com.LoanManagement.WalletSystem.util.SecurityUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for Authentication endpoints
 * Handles user authentication, registration, and account management
 * All endpoints except register and login require JWT authentication
 */
@RestController
@RequestMapping("/api/auth")
@Validated
public class AuthController {

    private final AuthService authService;
    private final SecurityUtil securityUtil;

    public AuthController(AuthService authService, SecurityUtil securityUtil) {
        this.authService = authService;
        this.securityUtil = securityUtil;
    }

    /**
     * POST /api/auth/register
     * Register a new user account
     * Public endpoint - no authentication required
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse userResponse = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
    }

    /**
     * POST /api/auth/login
     * Authenticate user and return JWT token
     * Public endpoint - no authentication required
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse authResponse = authService.login(request);
        return ResponseEntity.ok(authResponse);
    }

    /**
     * POST /api/auth/logout
     * Logout user by blacklisting their token
     * Requires JWT authentication
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.getToken());
        Map<String, String> response = new HashMap<>();
        response.put("message", "Logout successful");
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/recover-password
     * Initiate password recovery - sends OTP to email
     * Public endpoint - no authentication required
     */
    @PostMapping("/recover-password")
    public ResponseEntity<OtpResponse> recoverPassword(@Valid @RequestBody PasswordRecoveryRequest request) {
        OtpResponse response = authService.recoverPassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/resend-otp
     * Resend OTP for password recovery
     * Public endpoint - no authentication required
     */
    @PostMapping("/resend-otp")
    public ResponseEntity<OtpResponse> resendOtp(@Valid @RequestBody PasswordRecoveryRequest request) {
        OtpResponse response = authService.resendOtp(request);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/reset-password
     * Reset password using OTP
     * Public endpoint - no authentication required
     */
    @PostMapping("/reset-password")
    public ResponseEntity<AuthResponse> resetPassword(@Valid @RequestBody PasswordResetRequest request) {
        AuthResponse response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    /**
     * PATCH /api/auth/profile
     * Update user profile information
     * Requires JWT authentication
     */
    @PatchMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(@Valid @RequestBody UpdateProfileRequest request) {
        String userEmail = securityUtil.getCurrentUserEmail();
        UserResponse response = authService.updateProfile(userEmail, request);
        return ResponseEntity.ok(response);
    }
}


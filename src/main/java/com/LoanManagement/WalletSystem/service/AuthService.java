package com.LoanManagement.WalletSystem.service;

import com.LoanManagement.WalletSystem.dto.Auth.*;

public interface AuthService {
    /**
     * Register a new user
     */
    UserResponse register(RegisterRequest request);

    /**
     * Authenticate user and return JWT token
     */
    AuthResponse login(LoginRequest request);

    /**
     * Logout user by blacklisting their token
     */
    void logout(String token);

    /**
     * Initiate password recovery - sends OTP to email
     */
    OtpResponse recoverPassword(PasswordRecoveryRequest request);

    /**
     * Resend OTP for password recovery
     */
    OtpResponse resendOtp(PasswordRecoveryRequest request);

    /**
     * Reset password using OTP
     */
    AuthResponse resetPassword(PasswordResetRequest request);

    /**
     * Update user profile
     */
    UserResponse updateProfile(String userEmail, UpdateProfileRequest request);
}


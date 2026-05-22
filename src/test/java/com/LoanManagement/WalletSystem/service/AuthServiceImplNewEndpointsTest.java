package com.LoanManagement.WalletSystem.service;

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
import com.LoanManagement.WalletSystem.service.EmailService;
import com.LoanManagement.WalletSystem.service.OtpService;
import com.LoanManagement.WalletSystem.service.TokenBlacklistService;
import com.LoanManagement.WalletSystem.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthService Tests for New Endpoints")
class AuthServiceImplNewEndpointsTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserMapper userMapper;

    @Mock
    private OtpService otpService;

    @Mock
    private EmailService emailService;

    @Mock
    private TokenBlacklistService tokenBlacklistService;

    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private String testEmail;
    private String testToken;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testToken = "test-jwt-token";

        testUser = new User();
        testUser.setId(UUID.randomUUID().toString());
        testUser.setEmail(testEmail);
        testUser.setFullName("John Doe");
        testUser.setPhone("08000000000");
        testUser.setPassword("hashed_password");
        testUser.setCreatedAt(Instant.now());
    }

    // ======================== Logout Tests ========================

    @Test
    @DisplayName("Should logout successfully by blacklisting token")
    void testLogoutSuccessfully() {
        // Arrange
        when(jwtUtil.getSubject(testToken)).thenReturn(testEmail);
        when(jwtUtil.getExpirationDate(testToken)).thenReturn(new Date(System.currentTimeMillis() + 3600000));

        // Act
        authService.logout(testToken);

        // Assert
        verify(jwtUtil, times(1)).getSubject(testToken);
        verify(jwtUtil, times(1)).getExpirationDate(testToken);
        verify(tokenBlacklistService, times(1)).blacklistToken(eq(testToken), eq(testEmail), anyLong());
    }

    @Test
    @DisplayName("Should extract correct email from token during logout")
    void testLogoutExtractsCorrectEmail() {
        // Arrange
        when(jwtUtil.getSubject(testToken)).thenReturn(testEmail);
        when(jwtUtil.getExpirationDate(testToken)).thenReturn(new Date(System.currentTimeMillis() + 3600000));

        // Act
        authService.logout(testToken);

        // Assert
        verify(tokenBlacklistService, times(1)).blacklistToken(eq(testToken), eq(testEmail), anyLong());
    }

    // ======================== Password Recovery Tests ========================

    @Test
    @DisplayName("Should recover password and send OTP successfully")
    void testRecoverPasswordSuccessfully() {
        // Arrange
        PasswordRecoveryRequest request = new PasswordRecoveryRequest(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(otpService.generateOtp(testEmail)).thenReturn("123456");
        when(otpService.getRemainingOtpTime(testEmail)).thenReturn(600L);

        // Act
        OtpResponse response = authService.recoverPassword(request);

        // Assert
        assertNotNull(response);
        assertEquals(testEmail, response.getEmail());
        assertEquals(600L, response.getExpiresIn());
        verify(userRepository, times(1)).findByEmail(testEmail);
        verify(otpService, times(1)).generateOtp(testEmail);
        verify(emailService, times(1)).sendOtpEmail(testEmail, "123456");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException if user not found during password recovery")
    void testRecoverPasswordUserNotFound() {
        // Arrange
        PasswordRecoveryRequest request = new PasswordRecoveryRequest(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> authService.recoverPassword(request));
        verify(userRepository, times(1)).findByEmail(testEmail);
        verify(otpService, never()).generateOtp(any());
    }

    // ======================== Resend OTP Tests ========================

    @Test
    @DisplayName("Should resend OTP successfully")
    void testResendOtpSuccessfully() {
        // Arrange
        PasswordRecoveryRequest request = new PasswordRecoveryRequest(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(otpService.generateOtp(testEmail)).thenReturn("654321");
        when(otpService.getRemainingOtpTime(testEmail)).thenReturn(600L);

        // Act
        OtpResponse response = authService.resendOtp(request);

        // Assert
        assertNotNull(response);
        assertEquals(testEmail, response.getEmail());
        verify(otpService, times(1)).generateOtp(testEmail);
        verify(emailService, times(1)).sendOtpEmail(testEmail, "654321");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException if user not found during resend OTP")
    void testResendOtpUserNotFound() {
        // Arrange
        PasswordRecoveryRequest request = new PasswordRecoveryRequest(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> authService.resendOtp(request));
    }

    // ======================== Reset Password Tests ========================

    @Test
    @DisplayName("Should reset password successfully with valid OTP")
    void testResetPasswordSuccessfully() {
        // Arrange
        PasswordResetRequest request = new PasswordResetRequest(testEmail, "123456", "newPassword123");
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(otpService.verifyOtp(testEmail, "123456")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("hashed_new_password");
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(jwtUtil.generateToken(testEmail)).thenReturn(testToken);

        // Act
        AuthResponse response = authService.resetPassword(request);

        // Assert
        assertNotNull(response);
        assertEquals(testToken, response.getToken());
        verify(otpService, times(1)).verifyOtp(testEmail, "123456");
        verify(otpService, times(1)).markOtpAsUsed(testEmail, "123456");
        verify(passwordEncoder, times(1)).encode("newPassword123");
        verify(emailService, times(1)).sendPasswordResetConfirmationEmail(testEmail);
    }

    @Test
    @DisplayName("Should throw exception if OTP is invalid during reset password")
    void testResetPasswordInvalidOtp() {
        // Arrange
        PasswordResetRequest request = new PasswordResetRequest(testEmail, "invalid", "newPassword123");
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(otpService.verifyOtp(testEmail, "invalid")).thenReturn(false);

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> authService.resetPassword(request));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException if user not found during reset password")
    void testResetPasswordUserNotFound() {
        // Arrange
        PasswordResetRequest request = new PasswordResetRequest(testEmail, "123456", "newPassword123");
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> authService.resetPassword(request));
    }

    // ======================== Update Profile Tests ========================

    @Test
    @DisplayName("Should update profile successfully")
    void testUpdateProfileSuccessfully() {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest("Jane Doe", "08123456789", "12345678901");
        UserResponse mockResponse = new UserResponse();
        mockResponse.setFullName("Jane Doe");

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByPhone("08123456789")).thenReturn(false);
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toUserResponse(testUser)).thenReturn(mockResponse);

        // Act
        UserResponse response = authService.updateProfile(testEmail, request);

        // Assert
        assertNotNull(response);
        verify(userRepository, times(1)).findByEmail(testEmail);
        verify(userRepository, times(1)).save(testUser);
        assertEquals("Jane Doe", testUser.getFullName());
        assertEquals("08123456789", testUser.getPhone());
    }

    @Test
    @DisplayName("Should throw exception if new phone number is already in use")
    void testUpdateProfilePhoneAlreadyInUse() {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest("Jane Doe", "09000000000", "12345678901");
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(userRepository.existsByPhone("09000000000")).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> authService.updateProfile(testEmail, request));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException if user not found during update profile")
    void testUpdateProfileUserNotFound() {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest("Jane Doe", "08123456789", "12345678901");
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> authService.updateProfile(testEmail, request));
    }

    @Test
    @DisplayName("Should update profile with only required fields")
    void testUpdateProfileWithOnlyName() {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest("Jane Doe", null, null);
        UserResponse mockResponse = new UserResponse();

        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(testUser));
        when(userRepository.save(testUser)).thenReturn(testUser);
        when(userMapper.toUserResponse(testUser)).thenReturn(mockResponse);

        // Act
        UserResponse response = authService.updateProfile(testEmail, request);

        // Assert
        assertNotNull(response);
        assertEquals("Jane Doe", testUser.getFullName());
        verify(userRepository, times(1)).save(testUser);
    }
}


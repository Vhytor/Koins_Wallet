package com.LoanManagement.WalletSystem.controller;

import com.LoanManagement.WalletSystem.dto.Auth.*;
import com.LoanManagement.WalletSystem.service.AuthService;
import com.LoanManagement.WalletSystem.util.SecurityUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("AuthController New Endpoints Integration Tests")
class AuthControllerNewEndpointsTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private SecurityUtil securityUtil;

    private String testEmail;
    private String testToken;

    @BeforeEach
    void setUp() {
        testEmail = "test@example.com";
        testToken = "test-jwt-token";
    }

    // ======================== POST /api/auth/logout Tests ========================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should logout successfully with status 200")
    void testLogoutReturns200() throws Exception {
        // Arrange
        LogoutRequest request = new LogoutRequest(testToken);

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("Logout successful")));
    }

    @Test
    @DisplayName("Should return 403 when not authenticated for logout")
    void testLogoutNotAuthenticatedReturns403() throws Exception {
        // Arrange
        LogoutRequest request = new LogoutRequest(testToken);

        // Act & Assert
        mockMvc.perform(post("/api/auth/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // ======================== POST /api/auth/recover-password Tests ========================

    @Test
    @DisplayName("Should initiate password recovery and return 200")
    void testRecoverPasswordReturns200() throws Exception {
        // Arrange
        PasswordRecoveryRequest request = new PasswordRecoveryRequest(testEmail);
        OtpResponse mockResponse = new OtpResponse("OTP sent successfully", testEmail, 600L);

        when(authService.recoverPassword(request)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/recover-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(testEmail)))
                .andExpect(jsonPath("$.expiresIn", is(600)));
    }

    @Test
    @DisplayName("Should return 404 if user not found during password recovery")
    void testRecoverPasswordUserNotFoundReturns404() throws Exception {
        // Arrange
        PasswordRecoveryRequest request = new PasswordRecoveryRequest("nonexistent@example.com");

        when(authService.recoverPassword(request))
                .thenThrow(new com.LoanManagement.WalletSystem.exception.ResourceNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/recover-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should return 400 if email is invalid during password recovery")
    void testRecoverPasswordInvalidEmailReturns400() throws Exception {
        // Arrange
        PasswordRecoveryRequest request = new PasswordRecoveryRequest("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/auth/recover-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ======================== POST /api/auth/resend-otp Tests ========================

    @Test
    @DisplayName("Should resend OTP successfully and return 200")
    void testResendOtpReturns200() throws Exception {
        // Arrange
        PasswordRecoveryRequest request = new PasswordRecoveryRequest(testEmail);
        OtpResponse mockResponse = new OtpResponse("OTP resent successfully", testEmail, 600L);

        when(authService.resendOtp(request)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/resend-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message", is("OTP resent successfully")));
    }

    @Test
    @DisplayName("Should return 404 if user not found during resend OTP")
    void testResendOtpUserNotFoundReturns404() throws Exception {
        // Arrange
        PasswordRecoveryRequest request = new PasswordRecoveryRequest("nonexistent@example.com");

        when(authService.resendOtp(request))
                .thenThrow(new com.LoanManagement.WalletSystem.exception.ResourceNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/resend-otp")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    // ======================== POST /api/auth/reset-password Tests ========================

    @Test
    @DisplayName("Should reset password successfully and return JWT token")
    void testResetPasswordReturns200WithToken() throws Exception {
        // Arrange
        PasswordResetRequest request = new PasswordResetRequest(testEmail, "123456", "newPassword123");
        AuthResponse mockResponse = new AuthResponse(testToken);
        mockResponse.setTokenType("Bearer");

        when(authService.resetPassword(request)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", is(testToken)))
                .andExpect(jsonPath("$.tokenType", is("Bearer")));
    }

    @Test
    @DisplayName("Should return 400 if OTP is invalid during password reset")
    void testResetPasswordInvalidOtpReturns400() throws Exception {
        // Arrange
        PasswordResetRequest request = new PasswordResetRequest(testEmail, "invalid", "newPassword123");

        when(authService.resetPassword(request))
                .thenThrow(new com.LoanManagement.WalletSystem.exception.BusinessRuleException("Invalid OTP"));

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 400 if password is too short during password reset")
    void testResetPasswordShortPasswordReturns400() throws Exception {
        // Arrange
        PasswordResetRequest request = new PasswordResetRequest(testEmail, "123456", "short");

        // Act & Assert
        mockMvc.perform(post("/api/auth/reset-password")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // ======================== PATCH /api/auth/profile Tests ========================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should update profile successfully and return 200")
    void testUpdateProfileReturns200() throws Exception {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest("Jane Doe", "08123456789", "12345678901");
        UserResponse mockResponse = new UserResponse();
        mockResponse.setEmail(testEmail);
        mockResponse.setFullName("Jane Doe");

        when(securityUtil.getCurrentUserEmail()).thenReturn(testEmail);
        when(authService.updateProfile(testEmail, request)).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(patch("/api/auth/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email", is(testEmail)))
                .andExpect(jsonPath("$.fullName", is("Jane Doe")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should return 400 if phone is already in use")
    void testUpdateProfilePhoneAlreadyInUseReturns400() throws Exception {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest("Jane Doe", "08000000000", null);

        when(securityUtil.getCurrentUserEmail()).thenReturn(testEmail);
        when(authService.updateProfile(testEmail, request))
                .thenThrow(new com.LoanManagement.WalletSystem.exception.BusinessRuleException("Phone already in use"));

        // Act & Assert
        mockMvc.perform(patch("/api/auth/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Should return 403 when not authenticated for profile update")
    void testUpdateProfileNotAuthenticatedReturns403() throws Exception {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest("Jane Doe", "08123456789", null);

        // Act & Assert
        mockMvc.perform(patch("/api/auth/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should update only name without changing phone or BVN")
    void testUpdateProfileNameOnlyReturns200() throws Exception {
        // Arrange
        UpdateProfileRequest request = new UpdateProfileRequest("Jane Doe", null, null);
        UserResponse mockResponse = new UserResponse();
        mockResponse.setFullName("Jane Doe");

        when(securityUtil.getCurrentUserEmail()).thenReturn(testEmail);
        when(authService.updateProfile(eq(testEmail), any())).thenReturn(mockResponse);

        // Act & Assert
        mockMvc.perform(patch("/api/auth/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}


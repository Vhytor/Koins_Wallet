package com.LoanManagement.WalletSystem.service;

import com.LoanManagement.WalletSystem.dto.Auth.AuthResponse;
import com.LoanManagement.WalletSystem.dto.Auth.LoginRequest;
import com.LoanManagement.WalletSystem.dto.Auth.RegisterRequest;
import com.LoanManagement.WalletSystem.dto.Auth.UserResponse;
import com.LoanManagement.WalletSystem.exception.AuthenticationFailedException;
import com.LoanManagement.WalletSystem.exception.BusinessRuleException;
import com.LoanManagement.WalletSystem.mapper.UserMapper;
import com.LoanManagement.WalletSystem.model.Role;
import com.LoanManagement.WalletSystem.model.User;
import com.LoanManagement.WalletSystem.model.Wallet;
import com.LoanManagement.WalletSystem.repository.UserRepository;
import com.LoanManagement.WalletSystem.repository.WalletRepository;
import com.LoanManagement.WalletSystem.security.JwtUtil;
import com.LoanManagement.WalletSystem.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

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

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;
    private User testUser;
    private UserResponse userResponse;

    @BeforeEach
    void setUp() {
        // Setup test data
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("John Doe");
        registerRequest.setEmail("john@example.com");
        registerRequest.setPassword("password123");
        registerRequest.setPhone("1234567890");
        registerRequest.setBvn("12345678901");

        loginRequest = new LoginRequest();
        loginRequest.setEmail("john@example.com");
        loginRequest.setPassword("password123");

        testUser = new User();
        testUser.setId("user-id-1");
        testUser.setFullName("John Doe");
        testUser.setEmail("john@example.com");
        testUser.setPhone("1234567890");
        testUser.setPassword("hashed_password");
        testUser.setBvn("12345678901");
        testUser.setAccountStatus(1);
        testUser.setCreatedAt(Instant.now());
        testUser.setRoles(Collections.singleton(Role.ROLE_USER));

        userResponse = new UserResponse(
                "user-id-1",
                "John Doe",
                "john@example.com",
                "1234567890",
                1,
                Instant.now()
        );
    }

    // =============== REGISTRATION TESTS ===============

    @Test
    void testRegisterNewUserSuccessfully() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(registerRequest.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(walletRepository.save(any(Wallet.class))).thenReturn(new Wallet());
        when(userMapper.toUserResponse(testUser)).thenReturn(userResponse);

        // Act
        UserResponse result = authService.register(registerRequest);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getFullName());
        assertEquals("john@example.com", result.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
        verify(walletRepository, times(1)).save(any(Wallet.class));
        verify(userMapper, times(1)).toUserResponse(testUser);
    }

    @Test
    void testRegisterWithExistingEmail() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            authService.register(registerRequest);
        });
        assertEquals("Email already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void testRegisterWithExistingPhone() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(registerRequest.getPhone())).thenReturn(true);

        // Act & Assert
        BusinessRuleException exception = assertThrows(BusinessRuleException.class, () -> {
            authService.register(registerRequest);
        });
        assertEquals("Phone number already in use", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(walletRepository, never()).save(any(Wallet.class));
    }

    @Test
    void testRegisterWithNullPhone() {
        // Arrange
        registerRequest.setPhone(null);
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(walletRepository.save(any(Wallet.class))).thenReturn(new Wallet());
        when(userMapper.toUserResponse(testUser)).thenReturn(userResponse);

        // Act
        UserResponse result = authService.register(registerRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository, never()).existsByPhone(anyString());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterCreatesWalletWithZeroBalance() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(registerRequest.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(walletRepository.save(any(Wallet.class))).thenReturn(new Wallet());
        when(userMapper.toUserResponse(testUser)).thenReturn(userResponse);

        // Act
        UserResponse result = authService.register(registerRequest);

        // Assert
        assertNotNull(result);
        verify(walletRepository, times(1)).save(argThat(wallet ->
                wallet.getBalance().equals(BigDecimal.ZERO) &&
                wallet.getCurrency().equals("NGN") &&
                wallet.getUser().equals(testUser)
        ));
    }

    @Test
    void testRegisterSetUserRoleAsUserRole() {
        // Arrange
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(userRepository.existsByPhone(registerRequest.getPhone())).thenReturn(false);
        when(passwordEncoder.encode(registerRequest.getPassword())).thenReturn("hashed_password");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(walletRepository.save(any(Wallet.class))).thenReturn(new Wallet());
        when(userMapper.toUserResponse(testUser)).thenReturn(userResponse);

        // Act
        UserResponse result = authService.register(registerRequest);

        // Assert
        assertNotNull(result);
        verify(userRepository, times(1)).save(argThat(user ->
                user.getRoles().contains(Role.ROLE_USER) &&
                user.getRoles().size() == 1
        ));
    }

    // =============== LOGIN TESTS ===============

    @Test
    void testLoginSuccessfully() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(loginRequest.getEmail())).thenReturn("jwt-token-123");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token-123", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        verify(authenticationManager, times(1)).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtUtil, times(1)).generateToken(loginRequest.getEmail());
    }

    @Test
    void testLoginWithInvalidCredentials() {
        // Arrange
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Invalid credentials"));

        // Act & Assert
        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class, () -> {
            authService.login(loginRequest);
        });
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void testLoginWithGenericException() {
        // Arrange
        when(authenticationManager.authenticate(any())).thenThrow(new RuntimeException("Database connection failed"));

        // Act & Assert
        AuthenticationFailedException exception = assertThrows(AuthenticationFailedException.class, () -> {
            authService.login(loginRequest);
        });
        assertTrue(exception.getMessage().contains("Authentication failed"));
    }

    @Test
    void testLoginTokenTypeBearerIsSet() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken(loginRequest.getEmail())).thenReturn("jwt-token-123");

        // Act
        AuthResponse response = authService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Bearer", response.getTokenType());
    }

    @Test
    void testLoginUsesCorrectEmailForTokenGeneration() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtUtil.generateToken("john@example.com")).thenReturn("jwt-token-123");

        // Act
        authService.login(loginRequest);

        // Assert
        verify(jwtUtil, times(1)).generateToken("john@example.com");
    }
}


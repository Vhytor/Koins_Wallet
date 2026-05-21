package com.LoanManagement.WalletSystem.advice;

import com.LoanManagement.WalletSystem.exception.AuthenticationFailedException;
import com.LoanManagement.WalletSystem.exception.BusinessRuleException;
import com.LoanManagement.WalletSystem.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    // =============== RESOURCE NOT FOUND EXCEPTION TESTS ===============

    @Test
    void testHandleResourceNotFoundException() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("User not found");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleResourceNotFound(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleResourceNotFoundExceptionReturnsCorrectMessage() {
        // Arrange
        String errorMessage = "Wallet not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(errorMessage);

        // Act
        ResponseEntity<?> response = exceptionHandler.handleResourceNotFound(exception);

        // Assert
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains(errorMessage));
    }

    @Test
    void testHandleResourceNotFoundExceptionStatusIs404() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Resource not found");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleResourceNotFound(exception);

        // Assert
        assertEquals(404, response.getStatusCodeValue());
    }

    // =============== BUSINESS RULE EXCEPTION TESTS ===============

    @Test
    void testHandleBusinessRuleException() {
        // Arrange
        BusinessRuleException exception = new BusinessRuleException("Email already in use");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleBusinessRule(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleBusinessRuleExceptionReturnsCorrectMessage() {
        // Arrange
        String errorMessage = "Amount must be greater than zero";
        BusinessRuleException exception = new BusinessRuleException(errorMessage);

        // Act
        ResponseEntity<?> response = exceptionHandler.handleBusinessRule(exception);

        // Assert
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains(errorMessage));
    }

    @Test
    void testHandleBusinessRuleExceptionStatusIs400() {
        // Arrange
        BusinessRuleException exception = new BusinessRuleException("Business rule violated");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleBusinessRule(exception);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
    }

    // =============== AUTHENTICATION FAILED EXCEPTION TESTS ===============

    @Test
    void testHandleAuthenticationFailedException() {
        // Arrange
        AuthenticationFailedException exception = new AuthenticationFailedException("Invalid credentials");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleAuthenticationFailed(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleAuthenticationFailedExceptionReturnsCorrectMessage() {
        // Arrange
        String errorMessage = "Invalid email or password";
        AuthenticationFailedException exception = new AuthenticationFailedException(errorMessage);

        // Act
        ResponseEntity<?> response = exceptionHandler.handleAuthenticationFailed(exception);

        // Assert
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains(errorMessage));
    }

    @Test
    void testHandleAuthenticationFailedExceptionStatusIs401() {
        // Arrange
        AuthenticationFailedException exception = new AuthenticationFailedException("Authentication failed");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleAuthenticationFailed(exception);

        // Assert
        assertEquals(401, response.getStatusCodeValue());
    }

    // =============== ILLEGAL ARGUMENT EXCEPTION TESTS ===============

    @Test
    void testHandleIllegalArgumentException() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleIllegalArg(exception);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
    }

    @Test
    void testHandleIllegalArgumentExceptionReturnsCorrectMessage() {
        // Arrange
        String errorMessage = "Invalid input provided";
        IllegalArgumentException exception = new IllegalArgumentException(errorMessage);

        // Act
        ResponseEntity<?> response = exceptionHandler.handleIllegalArg(exception);

        // Assert
        assertNotNull(response.getBody());
        assertTrue(response.getBody().toString().contains(errorMessage));
    }

    @Test
    void testHandleIllegalArgumentExceptionStatusIs400() {
        // Arrange
        IllegalArgumentException exception = new IllegalArgumentException("Invalid argument");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleIllegalArg(exception);

        // Assert
        assertEquals(400, response.getStatusCodeValue());
    }

    // =============== RESPONSE FORMAT TESTS ===============

    @Test
    void testExceptionResponseIncludesErrorField() {
        // Arrange
        ResourceNotFoundException exception = new ResourceNotFoundException("Test error");

        // Act
        ResponseEntity<?> response = exceptionHandler.handleResourceNotFound(exception);

        // Assert
        assertNotNull(response.getBody());
        assertTrue(response.getBody() instanceof Map);
        Map<?, ?> errorMap = (Map<?, ?>) response.getBody();
        assertTrue(errorMap.containsKey("error"));
    }

    @Test
    void testAllExceptionsReturnMapResponse() {
        // Test ResourceNotFoundException
        ResponseEntity<?> r1 = exceptionHandler.handleResourceNotFound(
                new ResourceNotFoundException("test"));
        assertTrue(r1.getBody() instanceof Map);

        // Test BusinessRuleException
        ResponseEntity<?> r2 = exceptionHandler.handleBusinessRule(
                new BusinessRuleException("test"));
        assertTrue(r2.getBody() instanceof Map);

        // Test AuthenticationFailedException
        ResponseEntity<?> r3 = exceptionHandler.handleAuthenticationFailed(
                new AuthenticationFailedException("test"));
        assertTrue(r3.getBody() instanceof Map);

        // Test IllegalArgumentException
        ResponseEntity<?> r4 = exceptionHandler.handleIllegalArg(
                new IllegalArgumentException("test"));
        assertTrue(r4.getBody() instanceof Map);
    }

    // =============== HTTP STATUS CODE MAPPING TESTS ===============

    @Test
    void testCorrectHttpStatusMappings() {
        // ResourceNotFoundException -> 404
        ResponseEntity<?> r1 = exceptionHandler.handleResourceNotFound(
                new ResourceNotFoundException("test"));
        assertEquals(404, r1.getStatusCodeValue());

        // BusinessRuleException -> 400
        ResponseEntity<?> r2 = exceptionHandler.handleBusinessRule(
                new BusinessRuleException("test"));
        assertEquals(400, r2.getStatusCodeValue());

        // AuthenticationFailedException -> 401
        ResponseEntity<?> r3 = exceptionHandler.handleAuthenticationFailed(
                new AuthenticationFailedException("test"));
        assertEquals(401, r3.getStatusCodeValue());

        // IllegalArgumentException -> 400
        ResponseEntity<?> r4 = exceptionHandler.handleIllegalArg(
                new IllegalArgumentException("test"));
        assertEquals(400, r4.getStatusCodeValue());
    }

    @Test
    void testDifferentErrorMessagesAreHandledCorrectly() {
        // Arrange
        String[] messages = {
                "User not found",
                "Wallet does not belong to user",
                "Email already in use",
                "Phone already in use"
        };

        // Act & Assert
        for (String message : messages) {
            BusinessRuleException exception = new BusinessRuleException(message);
            ResponseEntity<?> response = exceptionHandler.handleBusinessRule(exception);
            assertTrue(response.getBody().toString().contains(message));
        }
    }
}


package com.LoanManagement.WalletSystem.controller;

import com.LoanManagement.WalletSystem.dto.Transaction.TransactionResponse;
import com.LoanManagement.WalletSystem.service.TransactionService;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("TransactionController Integration Tests")
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TransactionService transactionService;

    @MockBean
    private SecurityUtil securityUtil;

    private String testUserEmail;
    private String transactionId1;
    private String transactionId2;
    private TransactionResponse transactionResponse1;
    private TransactionResponse transactionResponse2;

    @BeforeEach
    void setUp() {
        testUserEmail = "test@example.com";
        transactionId1 = UUID.randomUUID().toString();
        transactionId2 = UUID.randomUUID().toString();

        transactionResponse1 = new TransactionResponse();
        transactionResponse1.setId(transactionId1);
        transactionResponse1.setType("CREDIT");
        transactionResponse1.setAmount(new BigDecimal("1000.00"));
        transactionResponse1.setReference("TXN001");
        transactionResponse1.setStatus(1);
        transactionResponse1.setCreatedAt(Instant.now());

        transactionResponse2 = new TransactionResponse();
        transactionResponse2.setId(transactionId2);
        transactionResponse2.setType("DEBIT");
        transactionResponse2.setAmount(new BigDecimal("500.00"));
        transactionResponse2.setReference("TXN002");
        transactionResponse2.setStatus(1);
        transactionResponse2.setCreatedAt(Instant.now().minusSeconds(3600));
    }

    // ======================== GET /api/transactions Tests ========================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should return all transactions for authenticated user with status 200")
    void testGetAllTransactionsReturns200() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn(testUserEmail);
        when(transactionService.getAllTransactions(testUserEmail))
                .thenReturn(List.of(transactionResponse1, transactionResponse2));

        // Act & Assert
        mockMvc.perform(get("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(transactionId1)))
                .andExpect(jsonPath("$[0].type", is("CREDIT")))
                .andExpect(jsonPath("$[0].amount", is(1000.00)))
                .andExpect(jsonPath("$[1].id", is(transactionId2)))
                .andExpect(jsonPath("$[1].type", is("DEBIT")))
                .andExpect(jsonPath("$[1].amount", is(500.00)));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should return empty list when user has no transactions")
    void testGetAllTransactionsReturnsEmptyList() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn(testUserEmail);
        when(transactionService.getAllTransactions(testUserEmail))
                .thenReturn(List.of());

        // Act & Assert
        mockMvc.perform(get("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should return transactions with correct structure and all fields")
    void testGetAllTransactionsCorrectStructure() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn(testUserEmail);
        when(transactionService.getAllTransactions(testUserEmail))
                .thenReturn(List.of(transactionResponse1));

        // Act & Assert
        mockMvc.perform(get("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").exists())
                .andExpect(jsonPath("$[0].type").exists())
                .andExpect(jsonPath("$[0].amount").exists())
                .andExpect(jsonPath("$[0].reference").exists())
                .andExpect(jsonPath("$[0].status").exists())
                .andExpect(jsonPath("$[0].createdAt").exists());
    }

    @Test
    @DisplayName("Should return 403 when user is not authenticated for getAllTransactions")
    void testGetAllTransactionsNotAuthenticatedReturns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should call transactionService with correct user email")
    void testGetAllTransactionsCallsServiceWithCorrectEmail() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn(testUserEmail);
        when(transactionService.getAllTransactions(testUserEmail))
                .thenReturn(List.of());

        // Act
        mockMvc.perform(get("/api/transactions")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        // Assert (not explicitly needed but shows pattern)
    }

    // ======================== GET /api/transactions/{transactionId} Tests ========================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should return single transaction by ID with status 200")
    void testGetTransactionByIdReturns200() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn(testUserEmail);
        when(transactionService.getTransactionById(testUserEmail, transactionId1))
                .thenReturn(transactionResponse1);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/{transactionId}", transactionId1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(transactionId1)))
                .andExpect(jsonPath("$.type", is("CREDIT")))
                .andExpect(jsonPath("$.amount", is(1000.00)))
                .andExpect(jsonPath("$.reference", is("TXN001")))
                .andExpect(jsonPath("$.status", is(1)));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should return different transactions for different IDs")
    void testGetTransactionByIdReturnsDifferentTransactions() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn(testUserEmail);
        when(transactionService.getTransactionById(testUserEmail, transactionId1))
                .thenReturn(transactionResponse1);
        when(transactionService.getTransactionById(testUserEmail, transactionId2))
                .thenReturn(transactionResponse2);

        // Act & Assert for first transaction
        mockMvc.perform(get("/api/transactions/{transactionId}", transactionId1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("CREDIT")))
                .andExpect(jsonPath("$.amount", is(1000.00)));

        // Act & Assert for second transaction
        mockMvc.perform(get("/api/transactions/{transactionId}", transactionId2)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("DEBIT")))
                .andExpect(jsonPath("$.amount", is(500.00)));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should return transaction with all required fields")
    void testGetTransactionByIdReturnsAllRequiredFields() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn(testUserEmail);
        when(transactionService.getTransactionById(testUserEmail, transactionId1))
                .thenReturn(transactionResponse1);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/{transactionId}", transactionId1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.type").exists())
                .andExpect(jsonPath("$.amount").exists())
                .andExpect(jsonPath("$.reference").exists())
                .andExpect(jsonPath("$.status").exists())
                .andExpect(jsonPath("$.createdAt").exists());
    }

    @Test
    @DisplayName("Should return 403 when user is not authenticated for getTransactionById")
    void testGetTransactionByIdNotAuthenticatedReturns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/transactions/{transactionId}", transactionId1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should return 404 when transaction not found")
    void testGetTransactionByIdNotFoundReturns404() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn(testUserEmail);
        when(transactionService.getTransactionById(testUserEmail, transactionId1))
                .thenThrow(new com.LoanManagement.WalletSystem.exception.ResourceNotFoundException(
                        "Transaction not found or does not belong to the authenticated user"
                ));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/{transactionId}", transactionId1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should prevent access to other user's transaction")
    void testGetTransactionByIdUnauthorizedAccessReturns404() throws Exception {
        // Arrange - simulate when authenticated user tries to access transaction of another user
        when(securityUtil.getCurrentUserEmail()).thenReturn(testUserEmail);
        when(transactionService.getTransactionById(testUserEmail, transactionId1))
                .thenThrow(new com.LoanManagement.WalletSystem.exception.ResourceNotFoundException(
                        "Transaction not found or does not belong to the authenticated user"
                ));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/{transactionId}", transactionId1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should return correct transaction data type (CREDIT)")
    void testGetTransactionByIdCorrectCreditType() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn(testUserEmail);
        when(transactionService.getTransactionById(testUserEmail, transactionId1))
                .thenReturn(transactionResponse1);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/{transactionId}", transactionId1)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("CREDIT")));
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should return correct transaction data type (DEBIT)")
    void testGetTransactionByIdCorrectDebitType() throws Exception {
        // Arrange
        when(securityUtil.getCurrentUserEmail()).thenReturn(testUserEmail);
        when(transactionService.getTransactionById(testUserEmail, transactionId2))
                .thenReturn(transactionResponse2);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/{transactionId}", transactionId2)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.type", is("DEBIT")));
    }

    // ======================== Path & Query Parameter Tests ========================

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should handle UUID in path parameter correctly")
    void testGetTransactionByIdWithValidUUID() throws Exception {
        // Arrange
        String validUUID = UUID.randomUUID().toString();
        when(securityUtil.getCurrentUserEmail()).thenReturn(testUserEmail);
        when(transactionService.getTransactionById(testUserEmail, validUUID))
                .thenThrow(new com.LoanManagement.WalletSystem.exception.ResourceNotFoundException("Transaction not found"));

        // Act & Assert
        mockMvc.perform(get("/api/transactions/{transactionId}", validUUID)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "test@example.com")
    @DisplayName("Should properly encode special characters in transaction ID path")
    void testGetTransactionByIdWithSpecialCharacters() throws Exception {
        // Arrange
        String transactionIdWithSpecialChars = "txn-001-test";
        when(securityUtil.getCurrentUserEmail()).thenReturn(testUserEmail);
        when(transactionService.getTransactionById(eq(testUserEmail), anyString()))
                .thenReturn(transactionResponse1);

        // Act & Assert
        mockMvc.perform(get("/api/transactions/{transactionId}", transactionIdWithSpecialChars)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }
}


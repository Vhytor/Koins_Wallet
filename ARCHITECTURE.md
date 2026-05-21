# Architecture Refactoring Document

## Overview
This document explains the refactoring that was done to restructure the KOINS Wallet System to follow SOLID principles and clean architecture patterns.

## Problem Statement (Before Refactoring)

### Issues Identified:
1. **Controllers handling business logic**
   - Controllers were directly instantiating entities
   - Authentication logic was in the controller
   - DTO mapping was happening in controllers

2. **Weak separation of concerns**
   - Controllers accessing repositories directly
   - No clear dependency boundaries

3. **Poor exception handling**
   - Generic IllegalArgumentException everywhere
   - No HTTP status code mapping
   - Unclear error types

4. **Security context scattered**
   - `SecurityContextHolder` accessed everywhere
   - No centralized user extraction logic

5. **Direct entity exposure**
   - Entities returned from endpoints
   - DTOs created on-the-fly, inconsistently

## Solution Applied

### 1. Custom Exception Layer
Created domain-specific exceptions:

```
exception/
├── ResourceNotFoundException  (→ 404)
├── BusinessRuleException      (→ 400)
└── AuthenticationFailedException (→ 401)
```

**Benefits:**
- ✓ Clear error semantics
- ✓ Type-safe exception handling
- ✓ Proper HTTP status mapping in GlobalExceptionHandler

### 2. Service Interfaces & Implementations

#### Pattern: Interface → Implementation

```java
// Service Interface (contract)
public interface AuthService {
    UserResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
}

// Implementation (can be swapped)
@Service
public class AuthServiceImpl implements AuthService {
    // ... business logic
}
```

**Services Created:**
- `AuthService` - Authentication and registration
- `WalletService` - Wallet operations and funding
- `TransactionService` - Transaction persistence

**Benefits:**
- ✓ Dependency inversion (depend on abstractions)
- ✓ Easy to mock in tests
- ✓ Easy to swap implementations (e.g., payment gateway V1 → V2)
- ✓ Clear business logic boundaries

### 3. Mapper Layer (DTO Transformation)

```java
// Mapper Interface
public interface UserMapper {
    UserResponse toUserResponse(User user);
}

// Implementation
@Component
public class UserMapperImpl implements UserMapper {
    @Override
    public UserResponse toUserResponse(User user) {
        return new UserResponse(/* map fields */);
    }
}
```

**Mappers Created:**
- `UserMapper` - User → UserResponse
- `WalletMapper` - Wallet → WalletResponse
- `TransactionMapper` - Transaction → TransactionResponse (with list support)

**Benefits:**
- ✓ Centralized transformation logic
- ✓ Reusable across services
- ✓ Can be replaced with ModelMapper/MapStruct later
- ✓ Null safety and validation

### 4. Utility Layer - SecurityUtil

```java
@Component
public class SecurityUtil {
    public String getCurrentUserEmail() {
        // Centralized security context access
    }
    
    public boolean isAuthenticated() {
        // Centralized auth check
    }
}
```

**Benefits:**
- ✓ Single source of truth for security operations
- ✓ Easier to test and mock
- ✓ Reduced  `SecurityContextHolder` coupling

### 5. Refactored Controllers

**Before:**
```java
@PostMapping("/register")
public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
    // Building entity manually
    User user = new User();
    user.setFullName(req.getFullName());
    // ... manual field mapping
    
    // Direct service call
    User saved = userService.registerUser(user);
    
    // Manual DTO building
    return ResponseEntity.ok().body("User registered with id: " + saved.getId());
}
```

**After:**
```java
@PostMapping("/register")
public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
    // Delegate to service layer
    UserResponse userResponse = authService.register(request);
    
    // Return typed DTO
    return ResponseEntity.status(HttpStatus.CREATED).body(userResponse);
}
```

**Benefits:**
- ✓ Controllers are thin and focused on HTTP
- ✓ Strongly typed request/response
- ✓ Proper HTTP status codes
- ✓ Easier to read and maintain

### 6. Enhanced Service Layer

**Before:**
```java
public User registerUser(User user) {
    // Simple registration with basic validation
    if (userRepository.existsByEmail(user.getEmail())) {
        throw new IllegalArgumentException("Email already in use");
    }
    // ... save user
}
```

**After:**
```java
@Service
public class AuthServiceImpl implements AuthService {
    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // Comprehensive validation
        validateRegistration(request);
        
        // Create and persistence
        User user = createUser(request);
        User savedUser = userRepository.save(user);
        
        // Create wallet atomically
        createWallet(savedUser);
        
        // Return DTO (not entity)
        return userMapper.toUserResponse(savedUser);
    }
    
    private void validateRegistration(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Email already in use");
        }
        // ... more validation
    }
}
```

**Benefits:**
- ✓ Clear business logic organization
- ✓ Transactional guarantees
- ✓ Rich error semantics
- ✓ DTO returned (not entity)

### 7. Authorization in Services

**WalletService Example:**
```java
@Override
public WalletResponse getMyWallet(String userEmail) {
    // Get authenticated user
    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    // Get wallet for that user
    Wallet wallet = walletRepository.findByUserId(user.getId())
        .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));

    // Return mapped DTO
    return walletMapper.toWalletResponse(wallet);
}
```

**Benefits:**
- ✓ Authorization logic in service (not scattered)
- ✓ Clear resource ownership validation
- ✓ Prevents unauthorized access

## SOLID Principles Applied

### S - Single Responsibility
| Component | Responsibility |
|-----------|-----------------|
| AuthController | Handle HTTP requests/responses |
| AuthService | Authentication logic |
| UserMapper | User entity → UserResponse DTO |
| SecurityUtil | Security context operations |

### O - Open/Closed
- ✓ Services defined as interfaces (open for extension)
- ✓ New LoanService can be added without modifying existing code
- ✓ Exception handlers are extensible

### L - Liskov Substitution
- ✓ All service implementations are proper substitutes
- ✓ Exception hierarchy is substitutable in handlers
- ✓ Mappers can be replaced with better implementations

### I - Interface Segregation
- ✓ `AuthService` only auth methods
- ✓ `WalletService` only wallet methods
- ✓ `TransactionMapper` only transaction mapping

### D - Dependency Inversion
- ✓ Controllers depend on interfaces, not implementations
- ✓ Services injected via constructor (explicit  dependencies)
- ✓ No service locator or hidden dependencies

## Code Examples: Before vs After

### Example 1: Login Endpoint

**Before:**
```java
@PostMapping("/login")
public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
    try {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.getEmail(), req.getPassword())
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = jwtUtil.generateToken(req.getEmail());
        return ResponseEntity.ok(new AuthResponse(token));
    } catch (BadCredentialsException ex) {
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}
```

**After:**
```java
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
    AuthResponse authResponse = authService.login(request);
    return ResponseEntity.ok(authResponse);
}

// In AuthServiceImpl
@Override
public AuthResponse login(LoginRequest request) {
    try {
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        String token = jwtUtil.generateToken(request.getEmail());
        AuthResponse response = new AuthResponse(token);
        response.setTokenType("Bearer");
        return response;
    } catch (BadCredentialsException ex) {
        throw new AuthenticationFailedException("Invalid email or password");
    }
}
```

**Improvements:**
- ✓ Controller is 3 lines (vs 14)
- ✓ Business logic is encapsulated
- ✓ Proper exception type
- ✓ Strongly typed response

### Example 2: Fund Wallet Endpoint

**Before:**
```java
@PostMapping("/{walletId}/fund")
public ResponseEntity<?> fundWallet(@PathVariable String walletId, @Valid @RequestBody FundRequest req) {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();
    var user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
    Transaction tx = transactionService.fundWallet(user.getId(), walletId, req.getAmount(), req.getReference());
    
    // Manual DTO building
    TransactionResponse resp = new TransactionResponse();
    resp.setId(tx.getId());
    resp.setType(tx.getType().name());
    resp.setAmount(tx.getAmount());
    resp.setReference(tx.getReference());
    resp.setStatus(tx.getStatus());
    resp.setCreatedAt(tx.getCreatedAt());
    
    return ResponseEntity.ok(resp);
}
```

**After:**
```java
@PostMapping("/{walletId}/fund")
public ResponseEntity<TransactionResponse> fundWallet(
    @PathVariable String walletId,
    @Valid @RequestBody FundRequest request
) {
    String userEmail = securityUtil.getCurrentUserEmail();
    TransactionResponse transaction = walletService.fundWallet(userEmail, walletId, request);
    return ResponseEntity.ok(transaction);
}

// In WalletServiceImpl
@Override
@Transactional
public TransactionResponse fundWallet(String userEmail, String walletId, FundRequest request) {
    // Get user
    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new ResourceNotFoundException("User not found"));

    // Get wallet and validate ownership
    Wallet wallet = walletRepository.findById(walletId)
        .orElseThrow(() -> new ResourceNotFoundException("Wallet not found"));
    
    if (!wallet.getUser().getId().equals(user.getId())) {
        throw new BusinessRuleException("Wallet does not belong to the authenticated user");
    }

    // Validate amount
    if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
        throw new BusinessRuleException("Amount must be greater than zero");
    }

    // Create transaction & update wallet
    Transaction transaction = new Transaction();
    transaction.setWallet(wallet);
    transaction.setUserId(user.getId());
    transaction.setType(TransactionType.CREDIT);
    transaction.setAmount(request.getAmount());
    transaction.setReference(request.getReference());
    transaction.setStatus(1);

    wallet.setBalance(wallet.getBalance().add(request.getAmount()));
    walletRepository.save(wallet);

    Transaction savedTransaction = transactionRepository.save(transaction);
    return transactionMapper.toTransactionResponse(savedTransaction);
}
```

**Improvements:**
- ✓ Controller code is 6 lines (clean HTTP handling)
- ✓ All business logic in service
- ✓ Better validation with proper exceptions
- ✓ DTO mapping delegated to mapper
- ✓ Clear ownership validation
- ✓ Transaction management

## Testing Benefits

The new architecture makes testing much easier:

```java
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {
    @Mock private UserRepository userRepository;
    @Mock private WalletRepository walletRepository;
    @Mock private UserMapper userMapper;
    @InjectMocks private AuthServiceImpl authService;

    @Test
    public void testRegisterSuccess() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("newuser@example.com");
        // ...

        // Act
        UserResponse response = authService.register(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getEmail()).isEqualTo("newuser@example.com");
    }

    @Test
    public void testRegisterDuplicateEmail() {
        // Arrange
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> authService.register(request));
    }
}
```

**Benefits:**
- ✓ Easy to mock dependencies
- ✓ Can test business logic in isolation
- ✓ No need for database in unit tests
- ✓ Controllers are simple HTTP → DTO mapping (minimal test coverage needed)

## Migration Guide: Adding New Features

### How to Add a Loan Service (following the new pattern)

1. **Create the exception** (if needed):
```java
// exception/LoanException.java
public class LoanException extends RuntimeException { }
```

2. **Create the entity**:
```java
@Entity
public class Loan { /* fields */ }
```

3. **Create the DTOs**:
```java
@RequestMapping("/api/loans")
public class LoanRequest { /* input fields */ }
public class LoanResponse { /* output fields */ }
```

4. **Create the mapper interface & implementation**:
```java
public interface LoanMapper {
    LoanResponse toLoanResponse(Loan loan);
}

@Component
public class LoanMapperImpl implements LoanMapper { }
```

5. **Create the service interface & implementation**:
```java
public interface LoanService {
    LoanResponse applyForLoan(String userEmail, LoanApplicationRequest request);
    LoanResponse approveLoan(String loanId);
}

@Service
public class LoanServiceImpl implements LoanService {
    // Inject all dependencies via constructor
    @Override
    @Transactional
    public LoanResponse applyForLoan(String userEmail, LoanApplicationRequest request) {
        // Business logic with proper validation and exceptions
    }
}
```

6. **Create the controller**:
```java
@RestController
@RequestMapping("/api/loans")
public class LoanController {
    private final LoanService loanService;
    private final SecurityUtil securityUtil;

    @PostMapping("/apply")
    public ResponseEntity<LoanResponse> apply(@Valid @RequestBody LoanApplicationRequest request) {
        String userEmail = securityUtil.getCurrentUserEmail();
        LoanResponse loan = loanService.applyForLoan(userEmail, request);
        return ResponseEntity.ok(loan);
    }
}
```

**Key Pattern:**
- DTOs ← Mappers ← Service (business logic) ← Repository (data)
- Controller only calls service and converts to HTTP

## File Structure Summary

```
New Files Created:
exception/
├── ResourceNotFoundException.java
├── BusinessRuleException.java
└── AuthenticationFailedException.java

util/
└── SecurityUtil.java

mapper/
├── UserMapper.java
├── WalletMapper.java
├── TransactionMapper.java
└── impl/
    ├── UserMapperImpl.java
    ├── WalletMapperImpl.java
    └── TransactionMapperImpl.java

service/
├── AuthService.java (NEW interface)
├── WalletService.java (NEW interface)
└── impl/
    ├── AuthServiceImpl.java (NEW implementation)
    └── WalletServiceImpl.java (NEW implementation)

dto/
├── Auth/
│   ├── UserResponse.java (NEW)
│   └── ...
├── Wallet/
│   └── WalletResponse.java (NEW)
└── Transaction/
    └── ...

Modified Files:
advice/
└── GlobalExceptionHandler.java (ENHANCED)

controller/
├── AuthController.java (REFACTORED)
└── WalletController.java (REFACTORED)
```

## Checklist: Is Your Code Following This Architecture?

- [ ] Controllers have `@RestController` and delegate everything to services
- [ ] Controllers NEVER access repositories directly
- [ ] Controllers NEVER build DTOs manually (use mappers)
- [ ] Controllers NEVER contain business logic validation
- [ ] Services are defined as interfaces
- [ ] Services use custom exceptions (not IllegalArgumentException)
- [ ] Mappers convert entities to DTOs
- [ ] Entities are never exposed in API responses
- [ ] All dependencies injected via constructor
- [ ] SecurityContextHolder only accessed via SecurityUtil
- [ ] @Transactional on service methods that modify data

## Benefits Summary

| Aspect | Benefit |
|--------|---------|
| **Testability** | Easy to unit test with mocks |
| **Maintainability** | Clear separation of concerns |
| **Scalability** | Easy to add new features |
| **Reusability** | Services/mappers used across codebase |
| **Flexibility** | Easy to swap implementations |
| **Error Handling** | Semantic exceptions with proper HTTP mapping |
| **Security** | Authorization logic not scattered |
| **Documentation** | Code is self-documenting (interfaces, clear methods) |

---

**Last Updated**: May 19, 2026
**Pattern**: Clean Architecture + SOLID + DDD (Domain-Driven Design)


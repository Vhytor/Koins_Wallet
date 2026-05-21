# WalletSystem (KOINS Backend Assessment)

A Spring Boot-based fintech backend for wallets, loans, and transactions. This project follows SOLID principles and clean architecture patterns.

## Architecture Overview

### Clean Architecture Layers
```
Controller Layer (HTTP concern only)
    ↓
Service Layer (Business logic)
    ↓
Repository Layer (Data access)
    ↓
Database
```

### Key Design Patterns
- **Service-oriented architecture**: Controllers delegate to services
- **DTO pattern**: Entities are never exposed directly; DTOs are used for API responses
- **Mapper pattern**: Clean conversion between entities and DTOs
- **Interface-based services**: All services have interfaces for dependency injection and testability
- **Custom exceptions**: Domain-specific exceptions for better error handling
- **Dependency injection**: Spring's `@Autowired` to inject dependencies

### Project Structure
```
src/main/java/com/LoanManagement/WalletSystem/
├── controller/         # HTTP request handling only
├── service/           # Business logic (interfaces)
├── service/impl/      # Service implementations
├── repository/        # Data access layer
├── model/             # JPA entities
├── dto/               # DTOs (separated by domain)
├── mapper/            # Entity to DTO mappers
├── mapper/impl/       # Mapper implementations
├── security/          # JWT and authentication
├── config/            # Spring configuration
├── exception/         # Custom exceptions
├── util/              # Utility classes
└── advice/            # Global exception handler
```

## What Was Refactored

### Previous Issues Fixed:
1. **Controllers handling business logic** → Now only handle HTTP concerns
2. **Direct repository access in controllers** → Controllers delegate to services
3. **Manual DTO mapping in controllers** → Dedicated mapper layer
4. **Weak exception handling** → Custom exceptions with granular handling
5. **SecurityContextHolder scattered** → Centralized in SecurityUtil

### New Components Added:

#### Custom Exceptions
- `ResourceNotFoundException` - for 404 scenarios
- `BusinessRuleException` - for business logic violations
- `AuthenticationFailedException` - for auth failures

#### Service Interfaces & Implementations
- `AuthService` (interface) → `AuthServiceImpl`
  - Encapsulates registration and login logic
  - Auto-creates wallet on signup
  - Returns DTOs instead of entities
  
- `WalletService` (interface) → `WalletServiceImpl`
  - Get wallet details
  - Fund wallet with transaction creation
  - Get transaction history
  - Enforces user ownership (authorization)
  
- `TransactionService` - handles transaction persistence
- `UserService` - kept for backward compatibility

#### Mapper Layer
- `UserMapper` → `UserMapperImpl`
  - Converts User entity to UserResponse DTO
  
- `WalletMapper` → `WalletMapperImpl`
  - Converts Wallet entity to WalletResponse DTO
  
- `TransactionMapper` → `TransactionMapperImpl`
  - Converts Transaction entity to TransactionResponse DTO

#### Utility & Security
- `SecurityUtil` - Centralized security context access
  - `getCurrentUserEmail()` - Extract authenticated user email
  - `isAuthenticated()` - Check auth status

#### Enhanced Exception Handling
- Global exception handler now maps all custom exceptions
- Structured error responses (JSON)
- HTTP status codes aligned with REST standards

#### DTOs (New and Enhanced)
- `UserResponse` - User data for API responses
- `WalletResponse` - Wallet data for API responses
- `AuthResponse` - Login response with token and type

## Prerequisites
- Docker & Docker Compose installed
- Java 17 and Maven (if running locally)
- PowerShell (for example commands)

## Quick Start (Docker)

From project root:

```powershell
cd C:\Users\USER\IdeaProjects\WalletSystem
docker-compose up --build -d
```

Wait for both services to be healthy (check logs):
```powershell
docker-compose logs -f app
```

Application will be available at: `http://localhost:8080`

## Testing the API

### 1. Register a user (auto-creates wallet):
```powershell
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "fullName": "Alice Smith",
    "email": "alice@example.com",
    "password": "password123",
    "phone": "08010000000",
    "bvn": "12345678901"
  }'
```

### 2. Login and get JWT token:
```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"alice@example.com","password":"password123"}'
```

Response:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "Bearer"
}
```

### 3. Get user's wallet:
```powershell
$token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
curl -H "Authorization: Bearer $token" http://localhost:8080/api/wallets/me
```

### 4. Fund wallet (with wallet ID from previous response):
```powershell
curl -X POST http://localhost:8080/api/wallets/<walletId>/fund `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{
    "amount": 5000.00,
    "reference": "TOPUP-001"
  }'
```

### 5. View transaction history:
```powershell
curl -H "Authorization: Bearer $token" http://localhost:8080/api/wallets/<walletId>/transactions
```

## Environment Variables
- `SPRING_DATASOURCE_URL` - MySQL JDBC URL
- `SPRING_DATASOURCE_USERNAME` - Database user
- `SPRING_DATASOURCE_PASSWORD` - Database password
- `APP_JWT_SECRET` - JWT signing secret (min 32 chars for production)

## Run Locally (Without Docker)

### Option 1: With Local MySQL
1. Update `src/main/resources/application.properties` with your MySQL credentials
2. Run:
```powershell
cd C:\Users\USER\IdeaProjects\WalletSystem
.\mvnw.cmd spring-boot:run
```

### Option 2: With H2 In-Memory Database (Testing)
```powershell
$env:SPRING_PROFILES_ACTIVE = "h2"
.\mvnw.cmd spring-boot:run
```

## SOLID Principles Applied

### Single Responsibility
- Controllers handle HTTP only
- Services handle business logic
- Repositories handle data access
- Mappers handle transformations

### Open/Closed
- Services are interfaces (open for extension via new implementations)
- Exception handlers can be extended to handle new exception types
- Mappers defined as interfaces for easy replacement

### Liskov Substitution
- All service implementations properly substitute their interfaces
- Exception hierarchy allows polymorphic exception handling

### Interface Segregation
- Service interfaces are focused (AuthService, WalletService)
- Mappers have specific, single-purpose interfaces

### Dependency Inversion
- Services depend on abstractions (interfaces)
- Constructor injection ensures explicit dependencies
- No hidden dependencies in methods

## Testing the Architecture

### Example: Adding a New Feature (Loan Service)
1. Create `LoanService` interface
2. Create `LoanServiceImpl` with business logic
3. Create `LoanRequest` and `LoanResponse` DTOs
4. Create `LoanMapper` for entity conversion
5. Inject in `LoanController` which only handles HTTP

The architecture enforces separation of concerns automatically.

## Next Milestones

- [ ] Loan entity and endpoints (apply, approve, disburse, repay)
- [ ] Webhook integration (Paystack/Flutterwave)
- [ ] Scheduled jobs (loan reminders, mark overdue)
- [ ] Email notifications
- [ ] Swagger/OpenAPI documentation
- [ ] Unit and integration tests
- [ ] Flyway database migrations
- [ ] RabbitMQ/Kafka messaging (bonus)

## Build Verification

```powershell
# Clean build
cd C:\Users\USER\IdeaProjects\WalletSystem
.\mvnw.cmd clean package -DskipTests
```

Expected output: `BUILD SUCCESS`

## Support & Development

All dependencies are managed in `pom.xml`. Key libraries:
- Spring Boot 4.0.6
- Spring Security 6
- Spring Data JPA
- JWT (jjwt)
- MySQL Connector
- Lombok (optional, can be added for cleaner code)

---

**Architecture Status**: ✅ Clean architecture with SOLID principles applied
**Current Endpoints**: 2 (register, login)
**Protected Endpoints**: 3 (getMyWallet, fundWallet, getTransactionHistory)



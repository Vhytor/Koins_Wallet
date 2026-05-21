# KOINS Wallet System - API Testing & Development Guide

## Quick Start Guide

### Prerequisites
- Docker and Docker Compose installed
- PowerShell for Windows
- A REST client (curl, Postman, or VS Code REST Client)

### 1. Start the Application Stack

```powershell
cd C:\Users\USER\IdeaProjects\WalletSystem
docker-compose up --build -d
```

**Verify services are running:**
```powershell
docker-compose logs -f app
```

Wait for this message:
```
Started WalletSystemApplication in X seconds
```

The application will be available at: `http://localhost:8080`

### 2. Stop the Stack

```powershell
docker-compose down
```

To also remove data:
```powershell
docker-compose down -v
```

---

## Complete Workflow: User Registration → Funding → Transactions

### Step 1: Register a User

This endpoint auto-creates a wallet.

**Request:**
```powershell
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "fullName": "Alice Smith",
    "email": "alice@example.com",
    "password": "SecurePass123!",
    "phone": "08010000000",
    "bvn": "12345678901"
  }'
```

**Expected Response (201 Created):**
```json
{
  "id": "123e4567-e89b-12d3-a456-426614174000",
  "fullName": "Alice Smith",
  "email": "alice@example.com",
  "phone": "08010000000",
  "accountStatus": 1,
  "createdAt": "2026-05-19T23:07:00Z"
}
```

**Save the user ID** (you'll need it later).

---

### Step 2: Login to Get JWT Token

**Request:**
```powershell
$loginResponse = curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{
    "email": "alice@example.com",
    "password": "SecurePass123!"
  }' | ConvertFrom-Json

# Save token for future requests
$token = $loginResponse.accessToken
Write-Host "Token: $token"
```

**Expected Response (200 OK):**
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhbGljZUBleGFtcGxlLmNvbSIsImlhdCI6MTcxNjE3NzY4MCwiZXhwIjoxNzE2MTc3OTgwfQ.ABC123...",
  "tokenType": "Bearer"
}
```

**Save the token** - you'll need it for all protected endpoints.

---

### Step 3: Get User's Wallet

**Request:**
```powershell
$walletResponse = curl -H "Authorization: Bearer $token" `
  http://localhost:8080/api/wallets/me | ConvertFrom-Json

# Save wallet ID for funding
$walletId = $walletResponse.id
Write-Host "Wallet ID: $walletId"
Write-Host "Initial Balance: $($walletResponse.balance)"
```

**Expected Response (200 OK):**
```json
{
  "id": "234f5678-f90a-23e4-b567-527725285111",
  "userId": "123e4567-e89b-12d3-a456-426614174000",
  "balance": 0.00,
  "currency": "NGN",
  "status": 1,
  "createdAt": "2026-05-19T23:07:00Z"
}
```

---

### Step 4: Fund the Wallet

This simulates a payment gateway top-up.

**Request:**
```powershell
curl -X POST http://localhost:8080/api/wallets/$walletId/fund `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{
    "amount": 50000.00,
    "reference": "TOPUP-20260519-001"
  }'
```

**Expected Response (200 OK):**
```json
{
  "id": "345g6789-a01b-34f5-c678-638836396222",
  "type": "CREDIT",
  "amount": 50000.00,
  "reference": "TOPUP-20260519-001",
  "status": 1,
  "createdAt": "2026-05-19T23:07:02Z"
}
```

---

### Step 5: Fund Wallet Again

Let's create another transaction.

**Request:**
```powershell
curl -X POST http://localhost:8080/api/wallets/$walletId/fund `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{
    "amount": 25000.00,
    "reference": "TOPUP-20260519-002"
  }'
```

---

### Step 6: View Transaction History

**Request:**
```powershell
curl -H "Authorization: Bearer $token" `
  http://localhost:8080/api/wallets/$walletId/transactions
```

**Expected Response (200 OK):**
```json
[
  {
    "id": "456h7890-b12c-45a6-d789-749947407333",
    "type": "CREDIT",
    "amount": 25000.00,
    "reference": "TOPUP-20260519-002",
    "status": 1,
    "createdAt": "2026-05-19T23:07:04Z"
  },
  {
    "id": "345g6789-a01b-34f5-c678-638836396222",
    "type": "CREDIT",
    "amount": 50000.00,
    "reference": "TOPUP-20260519-001",
    "status": 1,
    "createdAt": "2026-05-19T23:07:02Z"
  }
]
```

---

## Error Scenarios & Responses

### Trying to Fund Without Authentication

**Request:**
```powershell
curl -X POST http://localhost:8080/api/wallets/wallet-id/fund `
  -H "Content-Type: application/json" `
  -d '{"amount": 1000, "reference": "test"}'
```

**Expected Response (403 Forbidden):**
```json
{
  "error": "Forbidden"
}
```

---

### Invalid Login Credentials

**Request:**
```powershell
curl -X POST http://localhost:8080/api/auth/login `
  -H "Content-Type: application/json" `
  -d '{"email":"alice@example.com","password":"WrongPassword"}'
```

**Expected Response (401 Unauthorized):**
```json
{
  "error": "Invalid email or password"
}
```

---

### Wallet Not Found

**Request:**
```powershell
curl -H "Authorization: Bearer $token" `
  http://localhost:8080/api/wallets/nonexistent-wallet-id/transactions
```

**Expected Response (404 Not Found):**
```json
{
  "error": "Wallet not found"
}
```

---

### Negative Amount Validation

**Request:**
```powershell
curl -X POST http://localhost:8080/api/wallets/$walletId/fund `
  -H "Authorization: Bearer $token" `
  -H "Content-Type: application/json" `
  -d '{"amount": -1000, "reference": "test"}'
```

**Expected Response (400 Bad Request):**
```json
{
  "error": "Amount must be greater than zero"
}
```

---

### Wallet Ownership Violation

**Scenario:** Try to access another user's wallet

1. Register another user:
```powershell
curl -X POST http://localhost:8080/api/auth/register `
  -H "Content-Type: application/json" `
  -d '{
    "fullName": "Bob Johnson",
    "email": "bob@example.com",
    "password": "BobPass123!"
  }'
```

2. Get Bob's wallet ID and try to access with Alice's token:
```powershell
$bobWalletId = "bob-wallet-id"
curl -H "Authorization: Bearer $token" `
  http://localhost:8080/api/wallets/$bobWalletId/transactions
```

**Expected Response (400 Bad Request):**
```json
{
  "error": "Wallet does not belong to the authenticated user"
}
```

---

## Testing with Postman

### Import Collection

1. Create a new Postman collection
2. Add these requests:

#### Request 1: Register
- **Method:** POST
- **URL:** `http://localhost:8080/api/auth/register`
- **Body (raw JSON):**
```json
{
  "fullName": "Test User",
  "email": "test@example.com",
  "password": "TestPass123!",
  "phone": "08010000001",
  "bvn": "12345678901"
}
```

#### Request 2: Login
- **Method:** POST
- **URL:** `http://localhost:8080/api/auth/login`
- **Header:** `Content-Type: application/json`
- **Body (raw JSON):**
```json
{
  "email": "test@example.com",
  "password": "TestPass123!"
}
```
- **Tests tab** (to save token):
```javascript
if (pm.response.code === 200) {
    var token = pm.response.json().accessToken;
    pm.environment.set("token", token);
}
```

#### Request 3: Get My Wallet
- **Method:** GET
- **URL:** `http://localhost:8080/api/wallets/me`
- **Header:** `Authorization: Bearer {{token}}`
- **Tests tab** (to save wallet ID):
```javascript
if (pm.response.code === 200) {
    var walletId = pm.response.json().id;
    pm.environment.set("walletId", walletId);
}
```

#### Request 4: Fund Wallet
- **Method:** POST
- **URL:** `http://localhost:8080/api/wallets/{{walletId}}/fund`
- **Header:** `Authorization: Bearer {{token}}`
- **Body (raw JSON):**
```json
{
  "amount": 10000.00,
  "reference": "FUND-001"
}
```

#### Request 5: Get Transaction History
- **Method:** GET
- **URL:** `http://localhost:8080/api/wallets/{{walletId}}/transactions`
- **Header:** `Authorization: Bearer {{token}}`

---

## Local Development (Without Docker)

### Option 1: With Local MySQL

1. **Start MySQL locally** (assuming MySQL is installed):
```powershell
# Start MySQL service (if not already running)
# On Windows, if installed as service:
net start MySQL80
```

2. **Create database:**
```sql
CREATE DATABASE koins_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

3. **Update application.properties:**
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/koins_db
spring.datasource.username=root
spring.datasource.password=your_mysql_password
```

4. **Run the application:**
```powershell
cd C:\Users\USER\IdeaProjects\WalletSystem
.\mvnw.cmd spring-boot:run
```

### Option 2: With H2 In-Memory Database

Perfect for quick testing, data is reset on each restart.

```powershell
cd C:\Users\USER\IdeaProjects\WalletSystem
$env:SPRING_PROFILES_ACTIVE = "h2"
.\mvnw.cmd spring-boot:run
```

---

## Logs & Debugging

### View Application Logs

```powershell
# Tail logs from app container
docker-compose logs -f app

# View logs from MySQL
docker-compose logs -f mysql

# View all logs
docker-compose logs -f
```

### Enable Debug Logging

Add to `application.properties`:
```properties
logging.level.com.LoanManagement.WalletSystem=DEBUG
logging.level.org.springframework.security=DEBUG
```

---

## Database Inspection

### Connect to MySQL

```powershell
# Access MySQL shell
docker exec -it koins-mysql mysql -uroot -proot koins_db

# Or use a MySQL client
mysql -h localhost -u root -proot koins_db
```

### Useful Queries

```sql
-- View all users
SELECT id, full_name, email, account_status, created_at FROM users;

-- View all wallets
SELECT id, user_id, balance, currency, status, created_at FROM wallets;

-- View all transactions
SELECT id, wallet_id, user_id, type, amount, status, reference, created_at 
FROM transactions 
ORDER BY created_at DESC;

-- Check transaction count per wallet
SELECT wallet_id, COUNT(*) as transaction_count, SUM(amount) as total_funded 
FROM transactions 
GROUP BY wallet_id;
```

---

## Performance Testing

### Load-test Fund Wallet Endpoint

```powershell
# Using Apache Bench (if installed)
ab -n 100 -c 10 -p request.json http://localhost:8080/api/wallets/$walletId/fund

# Or using a simple PowerShell loop
for ($i = 0; $i -lt 50; $i++) {
    $amount = (Get-Random -Minimum 1000 -Maximum 50000)
    curl -X POST http://localhost:8080/api/wallets/$walletId/fund `
      -H "Authorization: Bearer $token" `
      -H "Content-Type: application/json" `
      -d "{`"amount`": $amount, `"reference`": `"LOAD-TEST-$i`"}"
}
```

---

## Troubleshooting

### Port 8080 Already in Use

```powershell
# Find process using port 8080
Get-NetTCPConnection -LocalPort 8080

# Kill process
Get-Process -Id <PID> | Stop-Process -Force
```

### MySQL Connection Refused

```powershell
# Check if MySQL container is running
docker ps | Select-String mysql

# Start MySQL
docker-compose up mysql -d

# Check logs
docker-compose logs mysql
```

### JWT Token Expired

Create a new token using the login endpoint.

### Build Failures

```powershell
# Clean and rebuild
cd C:\Users\USER\IdeaProjects\WalletSystem
.\mvnw.cmd clean compile

# Rebuild Docker image
docker-compose build --no-cache
```

---

## Next Steps

Once the basic workflow is tested:

1. **Implement Loan endpoints** - apply, approve, disburse, repay
2. **Add Webhook integration** - Paystack/Flutterwave payment confirmation
3. **Implement Scheduler** - loan reminders, mark overdue
4. **Add Email Notifications** - use service like SendGrid or AWS SES
5. **Add Swagger documentation** - auto-generated API docs

---

**Last Updated:** May 19, 2026
**Version:** cleanarch-refactored


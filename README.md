# The Ledger Service

A production-grade, thread-safe financial transaction ledger built with Spring Boot that handles concurrent money transfers, maintains account consistency, and ensures idempotent operations across distributed systems.

## What This Does

The Ledger Service provides a robust, scalable backend for multi-user financial applications. It manages user accounts, handles concurrent money transfers between accounts, and guarantees data consistency even under high concurrency; preventing race conditions, duplicate transactions, and insufficient fund scenarios.

**Key Outcomes:**

- **Zero Race Conditions**: Pessimistic locking + distributed idempotency keys prevent duplicate transactions
- **Strict Data Consistency**: ACID transactions with automatic rollback on insufficient funds
- **Audit Trail**: Complete transaction history with sender/receiver tracking
- **Resilient**: Built-in retry logic with exponential backoff for transient failures
- **Production-Ready**: Docker containerized, Spring security-enabled, comprehensive validation

---

## Architecture

### Request Flow

```
HTTP Request (REST API)
    ↓
UserController (Route & Validate DTO)
    ↓
Service Layer (Business Logic & Persistence Decisions)
    ├─ Idempotency Key Check (Prevent Duplicate Requests)
    ├─ Authorization Check (User/Account Ownership)
    ├─ Business Validation (Sufficient Funds, Valid Operations)
    └─ Transaction Execution (ACID-Compliant)
    ↓
Repository Layer (Database Persistence)
    ├─ Pessimistic Locking (for write operations)
    └─ Standard Queries (for reads)
    ↓
HTTP Response (DTO)
```

### Service Responsibilities

| Service | Responsibility |
|---------|---|
| **UserService** | User lifecycle (create, read, update, delete) |
| **AccountService** | Account management, balance tracking, name updates |
| **TransactionService** | Money transfers, concurrent transaction handling, transaction history |

**Data Transfer Objects (DTOs):**

- Input DTOs: `CreationDTO`, `PatchDTO` (validated, deserialized from HTTP request body)
- Output DTOs: `ResponseDTO` (serialized back to client)
- Mappers: Convert between DTOs ↔ Domain Entities

### Persistence & Concurrency Model

#### Consistency Strategy

- **Pessimistic Locking**: Write operations lock accounts to prevent concurrent modifications
- **ACID Transactions**: All multi-step operations (money transfers) wrapped in database transactions
- **Distributed Idempotency**: Idempotency keys expire after 24 hours, preventing unintended retries

#### Concurrency Control

**Write Operations (Account Modifications, Money Transfers):**

```java
@Transactional
@Retryable(retryFor = { PessimisticLockingFailureException.class }, maxAttempts = 3)
```

- Lock accounts involved before modification
- Retry up to 3 times with exponential backoff (50ms → 150ms)
- Rollback on business rule violation (e.g., insufficient funds)

**Read Operations:**

- No locking required
- Snapshot isolation from committed data

#### Idempotency Mechanism

- Client sends `Idempotency-Key` header with every write request
- Service stores key with 24-hour expiration
- Duplicate requests with same key are rejected within 24 hours
- Expired keys are automatically cleaned up

#### Money Transfer Process

1. Validate idempotency key (reject if duplicate)
2. Acquire pessimistic lock on sender & receiver accounts
3. Validate sender has sufficient balance
4. Debit sender, credit receiver (in single transaction)
5. Record transaction for audit
6. Store idempotency key
7. Return response

---

## Features

- **RESTful API** for user, account, and transaction management
- **Concurrent Money Transfers** with strict consistency guarantees
- **Idempotent Operations** via distributed idempotency keys
- **Comprehensive Input Validation** using Jakarta Bean Validation
- **Transaction Retry Logic** with exponential backoff
- **Exception Handling** with domain-specific error responses
- **Docker Support** for containerized deployment
- **H2 Database** (dev) / Production-ready for PostgreSQL/MySQL

---

## Quick Start

### Prerequisites

- Java 21+
- Gradle 8+
- Docker (optional)

### Build

```bash
cd backend
./gradlew clean build
```

### Run Locally

```bash
./gradlew bootRun
```

Server starts on `http://localhost:8080`

### Run in Docker

```bash
export DB_PASSWORD="your-database-password"

# Build and run Docker image
docker build -t ledger-service .
docker run --rm \
  --add-host=host.docker.internal:host-gateway \
  -e DB_HOST=host.docker.internal \
  -e DB_PASSWORD="$DB_PASSWORD" \
  -p 8080:8080 \
  ledger-service
```

---

## 📡 API Overview

### User Management

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/users` | GET | List all users |
| `/users` | POST | Create user (requires `Idempotency-Key`) |
| `/users/{id}` | GET | Get user details |
| `/users/{id}` | PUT | Replace user |
| `/users/{id}` | PATCH | Partial update (requires `Idempotency-Key`) |
| `/users/{id}/remove` | DELETE | Remove user |

### Account Management

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/users/{id}/accounts` | GET | List user accounts |
| `/users/{id}/accounts` | POST | Create account (requires `Idempotency-Key`) |
| `/users/{id}/accounts/{accountId}` | GET | Get account details |
| `/users/{id}/accounts/{accountId}` | PATCH | Update account name (requires `Idempotency-Key`) |
| `/users/{id}/accounts/{accountId}/remove` | DELETE | Remove empty account |

### Transaction Management

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/users/{id}/accounts/{accountId}/money-transfer` | POST | Transfer money between accounts (requires `Idempotency-Key`, pessimistically locked) |
| `/users/{id}/accounts/{accountId}/transactions` | GET | List account transactions |
| `/users/{id}/accounts/{accountId}/transactions/{transactionId}` | GET | Get transaction details |

### Example: Create User

```bash
curl -X POST http://localhost:8080/users \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: unique-key-123" \
  -d '{
    "firstName": "John",
    "lastName": "Doe"
  }'
```

### Example: Transfer Money

```bash
curl -X POST http://localhost:8080/users/1/accounts/10/money-transfer \
  -H "Content-Type: application/json" \
  -H "Idempotency-Key: transfer-key-456" \
  -d '{
    "receiverId": 20,
    "amount": 100.00,
    "currency": "USD"
  }'
```

---

## Testing

Run the full test suite:

```bash
./gradlew test
```

Test coverage includes:

- Unit tests for services and mappers
- Integration tests for API endpoints
- Concurrency tests for race condition prevention

---

## 📦 Project Structure

```
backend/
├── src/
│   ├── main/java/com/github/wcordor/ledger/
│   │   ├── LedgerApplication.java          # Spring Boot entry point
│   │   ├── controller/                     # REST endpoints
│   │   ├── service/                        # Business logic
│   │   ├── repository/                     # Data access (JPA)
│   │   ├── entity/                         # Domain models
│   │   ├── dtos/                           # Request/response DTOs
│   │   ├── mapper/                         # DTO ↔ Entity conversion
│   │   ├── advice/                         # Global exception handlers
│   │   └── exception/                      # Custom exceptions
│   ├── resources/
│   │   └── application.properties          # Spring Boot config
│   └── test/                               # Unit & integration tests
├── build.gradle.kts                        # Gradle build definition
└── Dockerfile                              # Container image definition
```

---

## Key Design Decisions

### Why DTOs?

DTOs provide a clean separation between external API contracts and internal domain models. They enable:

- Validation at the boundary
- Independent evolution of API and domain
- Protection of sensitive fields (e.g., internal IDs)

### Why Pessimistic Locking?

For financial applications, pessimistic locking prevents race conditions more reliably than optimistic approaches:

- Guaranteed consistency of account balances
- No retry loops due to conflicts
- Predictable performance under high contention

### Why Idempotency Keys?

HTTP operations can fail after commit (network partition). Idempotency keys allow safe retries:

- Client sends same key on retry
- Service detects duplicate, returns cached response
- Prevents double-charging users

### Why Retry Logic?

Under high concurrency, pessimistic locks occasionally timeout. Exponential backoff allows transient failures to resolve:

- 50ms first retry, up to 150ms max delay
- 3 total attempts
- Automatic transaction rollback on business rule violations

---

## License

See [LICENSE](./LICENSE) for details.

---

## Development

Built with Spring Boot 3.x, Java 21, and tested for production reliability.

For issues, questions, or contributions, see the [BUG_LOG.md](./BUG_LOG.md) for known issues and workarounds.

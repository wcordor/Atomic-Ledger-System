# The Ledger Service

The Ledger Service is a Spring Boot backend project built to model financial account operations in a way that is both practical and technically interesting. The project focuses on building a reliable API for managing users, accounts, and transfers while demonstrating core backend concepts such as transaction safety, idempotency, and concurrency-aware business logic.

This project was created as a portfolio piece to highlight my approach to backend development, especially around API design, persistence, and handling real-world reliability concerns in Java.

## What the project demonstrates

- A RESTful backend for creating and managing users and accounts
- Safe money transfer flows with rollback behavior when a transfer cannot be completed
- Idempotency handling to reduce duplicate or repeated operations
- Transaction-aware service logic for consistent state updates
- Spring Data JPA integration with PostgreSQL

## Project highlights

This project is designed to show that I can think beyond simple CRUD work and build systems that behave correctly when data integrity and concurrent operations matter. It is especially relevant for roles focused on backend development, API engineering, or software engineering fundamentals.

## Tech stack

- Java 21
- Spring Boot 4.0.5
- Spring Web
- Spring Data JPA
- PostgreSQL
- Gradle
- JUnit 5

## Project structure

- backend: Spring Boot application and REST API
  - controllers for users, accounts, and transactions
  - services for business logic and transaction handling
  - repositories for persistence
  - automated tests for core behavior

## Getting started

### Prerequisites

- Java 21
- PostgreSQL running locally
- Gradle

### Database configuration

The application expects PostgreSQL to be available at:

- host: localhost
- port: 5432
- database: postgres
- user: postgres
- password: my-password

These values are defined in backend/src/main/resources/application.properties.

### Run the backend

From the repository root:

```bash
cd backend
./gradlew bootRun
```

The application will start on port 8080.

## API overview

The backend currently exposes the following main endpoints:

### Users

- GET /users
- POST /users
- GET /users/{id}
- PATCH /users/{id}
- DELETE /users/{id}/remove
- GET /users/{id}/accounts

### Accounts

- GET /users/{id}/accounts
- GET /users/{id}/accounts/{accountId}
- POST /users/{id}/accounts
- PATCH /users/{id}/accounts/{accountId}
- DELETE /users/{id}/accounts/{accountId}/remove

### Transactions

- POST /users/{id}/accounts/{accountId}/money-transfer
- GET /users/{id}/accounts/{accountId}/transactions
- GET /users/{id}/accounts/{accountId}/transactions/{transactionId}

## Example behaviors

The project includes logic for:

- transferring funds between accounts
- rejecting transfers when the sender does not have enough funds
- rolling back transactions safely when a transfer fails
- preventing duplicate request behavior through idempotency handling

## Testing

Run the backend test suite with:

```bash
cd backend
./gradlew test
```

## Notes

This project is intentionally focused on demonstrating core backend engineering principles, including reliable service design, transactional correctness, and practical API development. It serves as a clear example of how I approach building dependable software systems with attention to data integrity and user-facing behavior.

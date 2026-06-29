# Atomic Ledger System

A compact full-stack demo that connects a Java Spring Boot backend with an Android client to model financial transfers safely and reliably.

## What this project demonstrates

- A backend API that persists users, accounts, and transfer operations in PostgreSQL.
- A mobile client that sends transfer requests and can trigger activity from the device accelerometer.
- Transaction safety and rollback behavior for failed transfers.
- Clear separation between backend business logic and client-side networking/UI concerns.

## Project structure

- `backend`: Spring Boot service with REST controller, JPA repositories, and transfer business logic.
- `android-client`: Android app with a single activity that posts transfer requests.

## How to run

1. Start PostgreSQL.
2. In `backend`, run:

   ```bash
   ./gradlew bootRun
   ```

3. Run the Android app from `android-client`.
   - The app is currently configured to call `http://172.25.216.231:8080/transfer`.
   - Update the URL in `android-client/app/src/main/java/com/example/atomicledgersystem/MainActivity.java` if your backend uses a different host.

## Backend details

- `backend/src/main/java/clean/S1Application.java` seeds sample users and accounts at startup.
- `backend/src/main/java/clean/TransferController.java` exposes a POST `/transfer` endpoint.
- `backend/src/main/java/clean/TransferService.java` performs the transfer with:
  - `@Transactional(rollbackOn = InsufficientFundsException.class)` for atomic rollback
  - `@Retryable(retryFor = RuntimeException.class, maxAttempts = 3)` for transient error handling
  - optimistic locking through `AccountRepo.findWithLockingById`
- `backend/src/main/java/clean/TransferRequest.java` expects JSON with `outAccId`, `inAccId`, and `amt`.

## Android client details

- `android-client/app/src/main/java/com/example/atomicledgersystem/MainActivity.java` registers an accelerometer listener.
- When movement magnitude exceeds `12.0f`, it posts a transfer request to the backend.
- The client alternates between a normal transfer amount (`10.00`) and a forced failure amount (`1000000.00`).
- `OkHttp` is used for network requests, and UI updates are performed on the main thread via `runOnUiThread(...)`.

## Configuration

From `backend/src/main/resources/application.properties`:

- `spring.datasource.url=jdbc:postgresql://localhost:5432/postgres`
- `spring.datasource.username=postgres`
- `spring.datasource.password=my-password`
- `spring.jpa.hibernate.ddl-auto=create-drop`
- `spring.jpa.show-sql=true`
- `server.address=0.0.0.0`

## Notes

- The backend uses `create-drop`, so the database schema and sample data are recreated on each start.
- The current mobile app setup is best suited for local development or demo purposes.
- The system is intentionally simple and focused on illustrating transactional transfer behavior, not production-ready security or validation.

## Potential improvements

- Move the backend URL out of the Android app and into configuration.
- Add proper authentication and input validation.
- Replace `create-drop` with a more stable migration strategy.
- Add more user-facing UI and account management features.

### android-client — student final project

Short student final-project Android app demonstrating posting transfer requests to the backend.

- **Tech:** Java, Android SDK, Gradle (Kotlin DSL)
- **Status:** Individual student project — created for a class final project but currently unmaintained.

How to run:

- Open the `android-client` folder in Android Studio and run the `app` module.
- Or build from the command line:
  `cd android-client`
  `./gradlew assembleDebug`

Notes:

- Update the backend host URL in `android-client/app/src/main/java/com/example/atomicledgersystem/MainActivity.java` if your backend runs on a different host.
- Kept for historical/reference purposes only; not actively maintained.

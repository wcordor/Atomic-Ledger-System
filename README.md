# Atomic-Ledger-System
A full-stack financial ledger implementing atomic operations and persistence.

## Project Structure
- **/backend**: Java Spring Boot API (Spring Data JPA, PostgreSQL)
- **/android-client**: Android Mobile App (OkHttp, Java)

## How to Run
1. Start PostgreSQL.
2. Run backend: `./gradlew bootRun` inside /backend.
3. Run Android Emulator and point to the host IP (10.0.2.2 or WSL IP).

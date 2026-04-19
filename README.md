# Atomic-Ledger-System
A full-stack financial ledger implementing atomic operations and persistence.

## Project Structure
- **/backend**: Java Spring Boot API (Spring Data JPA, PostgreSQL)
- **/android-client**: Android Mobile App (OkHttp, Java)

## How to Run
1. Start PostgreSQL.
2. Run backend: `./gradlew bootRun` inside /backend.
3. Run Android Emulator and point to the host IP (10.0.2.2 or WSL IP).

## Latest Update: Hardware Integration & Concurrency

### Shake-to-Sync Feature
The application now utilizes the device's physical **Accelerometer** to trigger a ledger synchronization. This demonstrates the interaction between the Android OS hardware abstraction layer and the application logic.

#### Technical Implementation:
* **Vector Magnitude Algorithm**: Calculates movement intensity using $$\sqrt{x^2 + y^2 + z^2}$$to ensure the sync only triggers above a$$12 \text{ m/s}^2$$ threshold.
* **Asynchronous I/O**: Network requests are handled via `OkHttp` callbacks, preventing the Main UI thread from blocking (Deadlock avoidance).
* **Thread Synchronization**: Implements `runOnUiThread` to safely pass data from the background network thread to the UI thread, satisfying OS concurrency requirements.

#### Hardware Interfacing:
* **SensorManager**: Interfaces with the OS to register/unregister listeners for the `TYPE_ACCELEROMETER` sensor.

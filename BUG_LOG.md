# Bug Log: Ledger Engine Development

This log serves as a comprehensive technical record of all architectural hurdles, runtime exceptions, and logic errors encountered and resolved throughout the development lifecycle of the ledger system.

| Issue ID | Category | Symptom / Error Message | Root Cause | Resolution |
| :--- | :--- | :--- | :--- | :--- |
| **BUG-001** | Dependency Injection | `100.00 != 30.00` state mismatch during component verification. | Target service instance was instantiated manually instead of utilizing Spring's IoC container. | Applied `@Autowired` to properly inject the managed bean. |
| **BUG-002** | Concurrency | `ObjectOptimisticLockingFailureException: Unexpected row count (expected 1 but was 0)` on Account updates. | Concurrent threads overwrote row versions simultaneously without explicit lock acquisition. | Implemented explicit version checking by adding `@Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)` to the repository layer. |
| **BUG-003** | Transaction Management | `InvalidDataAccessApiUsageException: No active transaction` | Entity state detached across asynchronous thread boundaries due to passing raw object references instead of IDs. | Refactored the method signature to `transferMoney(Long toId, Long fromId, BigDecimal amt)` and delegated entity fetching to the transaction-bound service layer. |
| **BUG-004** | Fault Tolerance | `ObjectOptimisticLockingFailureException` recurring on secondary thread executions. | The system did not attempt to recover when an optimistic locking collision naturally occurred during racing threads. | Annotated the boundary method with `@Retryable` to automatically re-attempt the transaction upon collision. |
| **BUG-005** | Build Management | `Unresolved dependency: org.springframework.retry` | Declared the dependency artifact configuration without specifying a fixed, compatible version string in `build.gradle.kts`. | Defined the explicit version coordinate: `implementation("org.springframework.retry:spring-retry:2.0.12")`. |
| **BUG-006** | Persistence | `100.00 != 20.00` balance assertion failure. | Test evaluated stale in-memory entity snapshots rather than pulling flushed changes directly from the persistence context. | Switched assertions to forcefully fetch the absolute latest database state via `repository.findById()`. |
| **BUG-007** | Async Testing | Non-deterministic test failures (`assertEquals` mismatch) during low-funds multi-threaded simulations. | Non-deterministic OS thread scheduling created multiple valid race-condition outcomes ($\$100/\$900$ or $\$200/\$800$), making rigid equality assertions flake. | Refactored assertions to use logical `OR` conditions wrapped in scale-insensitive `BigDecimal.compareTo() == 0` validations. |

---

## Technical Insights & Lessons Learned

### 1. Architectural Boundary Separation

Passing database entities directly into asynchronous workers (`CompletableFuture`) introduces severe transaction-detachment issues. Always pass entity primary keys (`Long id`) across thread boundaries and allow the receiving, `@Transactional`-managed service method to resolve the data within its own active persistence context.

### 2. Asserting Non-Deterministic Systems

When validating concurrent behavior under scarcity (e.g., draining an account simultaneously), hardcoded state assertions are anti-patterns. Tests must accommodate all mathematically sound race-condition paths governed by the underlying thread scheduler, ensuring invariants are met without demanding a single, rigid execution sequence.

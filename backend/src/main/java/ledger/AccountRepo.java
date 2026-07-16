package ledger;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface AccountRepo extends JpaRepository<Account, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findWithLockingById(Long id);

    List<Account> findByName(String name);
    Account findById(long id);
    List<Account> findByUserLastName(String lastName);
    List<Account> findByCurrency(String currency);
    @EntityGraph(attributePaths = {"sent", "received"})
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findWithTransactions(@Param("id") Long id);
}

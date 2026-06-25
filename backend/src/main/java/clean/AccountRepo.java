package clean;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.repository.CrudRepository;

import jakarta.persistence.LockModeType;

public interface AccountRepo extends CrudRepository<Account, Long> {
    
    /*@Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "10000")}) // 10s timeout*/
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    Optional<Account> findWithLockingById(Long id);

    List<Account> findByName(String name);
    Account findById(long id);
    List<Account> findByUserLastName(String lastName);
    List<Account> findByCurrency(String currency);
}

package clean;

import java.util.List;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface AccountRepo extends CrudRepository<Account, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "10000")}) // 10s timeout

    List<Account> findByName(String name);
    Account findById(long id);
    List<Account> findByUserLastName(String lastName);
    List<Account> findByCurrency(String currency);
}

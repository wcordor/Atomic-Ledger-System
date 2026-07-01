package clean;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;

public interface AccountRepo extends JpaRepository<Account, Long> {
    
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    Optional<Account> findWithLockingById(Long id);

    List<Account> findByName(String name);
    Account findById(long id);
    List<Account> findByUserLastName(String lastName);
    List<Account> findByCurrency(String currency);
    BigDecimal getBalanceById(long id);
}

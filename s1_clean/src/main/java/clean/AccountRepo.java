package clean;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface AccountRepo extends CrudRepository<Account, Long> {
    
    List<Account> findByName(String name);
    Account findById(long id);
    List<Account> findByUserLastName(String lastName);
    List<Account> findByCurrency(String currency);
}

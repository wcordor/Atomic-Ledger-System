package clean;

import java.util.List;

import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.CrudRepository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;

public interface UserRepo extends CrudRepository<User, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "10000")})
    List<User> findByLastName(String lastName);
    User findById(long id);
}
     
package clean;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

public interface UserRepo extends CrudRepository<User, Long> {

    /*@Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints({@QueryHint(name = "jakarta.persistence.lock.timeout", value = "10000")})
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    Optional<User> findWithLockingById(Long id);*/

    List<User> findByLastName(String lastName);
    User findById(long id);
}
     
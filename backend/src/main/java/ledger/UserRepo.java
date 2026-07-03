package ledger;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, Long> {

    List<User> findByLastName(String lastName);
    User findById(long id);
}
     
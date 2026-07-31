package com.github.wcordor.ledger;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    
    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public User changeName(Long id, String firstName, String lastName) {
        User user = repository.findWithLockingById(id).orElseThrow(() -> new UserNotFoundException(id));
        user.setFirstName(firstName);
        user.setLastName(lastName);
        
        return repository.save(user);
    }

    public List<User> getAll() {
        return repository.findAll();
    }

    public User saveUser(User user) {
        return repository.save(user);
    }

    public User getUser(Long id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public void deleteUser(Long id) {
        repository.deleteById(id);
    }

    @Transactional
    public User updateUser(Long id, Map<String, Object> updates) {
        User user = repository.findWithLockingById(id).orElseThrow(() -> new UserNotFoundException(id));

        updates.forEach((key, value) -> {
            switch (key) {
                case "firstName":
                    user.setFirstName((String) value);
                    break;
                case "lastName":
                    user.setLastName((String) value);
                    break;
            }
        });

        return repository.save(user);
    }
    
}

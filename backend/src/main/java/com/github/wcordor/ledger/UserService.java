package com.github.wcordor.ledger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    
    private final UserRepository repository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public UserService(UserRepository repository, IdempotencyKeyRepository idempotencyKeyRepository) {
        this.repository = repository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
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

    public User createUser(String idempotencyKey, String firstName, String lastName) {
        IdempotencyKey savedKey = idempotencyKeyRepository.findByKey(idempotencyKey).orElse(null);

        if (savedKey != null) {
            if (savedKey.getExpiryDate().isBefore(LocalDateTime.now())) {
                idempotencyKeyRepository.delete(savedKey);
            } else {
                throw new IdempotencyKeyAlreadyExistsException();
            }
        }

        User user = new User(firstName, lastName);

        IdempotencyKey newKey = new IdempotencyKey(idempotencyKey, LocalDateTime.now().plusHours(24));
        idempotencyKeyRepository.save(newKey);
        
        return repository.save(user);
    }

    public User getUser(Long id) {
        return repository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    public void deleteUser(Long id) {
        User user = getUser(id);

        if (user.getAccounts().size() == 0) {
            repository.deleteById(id);
        }
        else {
            throw new UserDeletionFailureException(id);
        }
        
    }

    @Transactional
    public User updateUser(String idempotencyKey, Long id, Map<String, Object> updates) {
        IdempotencyKey savedKey = idempotencyKeyRepository.findByKey(idempotencyKey).orElse(null);

        if (savedKey != null) {
            if (savedKey.getExpiryDate().isBefore(LocalDateTime.now())) {
                idempotencyKeyRepository.delete(savedKey);
            } else {
                throw new IdempotencyKeyAlreadyExistsException();
            }
        }

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

        IdempotencyKey newKey = new IdempotencyKey(idempotencyKey, LocalDateTime.now().plusHours(24));
        idempotencyKeyRepository.save(newKey);

        return repository.save(user);
    }
    
}

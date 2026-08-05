package com.github.wcordor.ledger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository, IdempotencyKeyRepository idempotencyKeyRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
    }

    public Account createAccount(String idempotencyKey, Long userId, String name, BigDecimal initialDeposit, String currency) {
        IdempotencyKey savedKey = idempotencyKeyRepository.findByKey(idempotencyKey).orElse(null);

        if (savedKey != null) {
            if (savedKey.getExpiryDate().isBefore(LocalDateTime.now())) {
                idempotencyKeyRepository.delete(savedKey);
            } else {
                throw new IdempotencyKeyAlreadyExistsException();
            }
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Account account = new Account(user, name, initialDeposit, currency);

        IdempotencyKey newKey = new IdempotencyKey(idempotencyKey, LocalDateTime.now().plusHours(24));
        idempotencyKeyRepository.save(newKey);
        
        return accountRepository.save(account);
    }
    
    public List<Account> getAccounts(Long userId) {
        return accountRepository.findByUser_Id(userId);
    }

    public Account getAccount(Long accountId, Long userId) {
        return accountRepository.findByIdAndUser_Id(accountId, userId).orElseThrow(() -> new AccountNotFoundException(accountId, userId));
    }

    @Transactional
    public Account changeName(String idempotencyKey, Long accountId, Long userId, String name) {
        IdempotencyKey savedKey = idempotencyKeyRepository.findByKey(idempotencyKey).orElse(null);

        if (savedKey != null) {
            if (savedKey.getExpiryDate().isBefore(LocalDateTime.now())) {
                idempotencyKeyRepository.delete(savedKey);
            } else {
                throw new IdempotencyKeyAlreadyExistsException();
            }
        }

        Account account = accountRepository.findWithLockingByIdAndUser_Id(accountId, userId)
            .orElseThrow(() -> new AccountNotFoundException(accountId, userId));

        account.setName(name);

        IdempotencyKey newKey = new IdempotencyKey(idempotencyKey, LocalDateTime.now().plusHours(24));
        idempotencyKeyRepository.save(newKey);
        
        return account;        
    }

    public void deleteAccount(Long accountId, Long userId) {
        Account account = getAccount(accountId, userId);
        if (account.getUserId() == userId && account.getBalance().compareTo(new BigDecimal("0.00")) == 0) {
            accountRepository.deleteById(accountId);
        }
        else {
            throw new AccountDeletionFailureException(accountId, userId);
        }
    }

}

package com.github.wcordor.ledger.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.wcordor.ledger.dtos.accountDTO.*;
import com.github.wcordor.ledger.entity.Account;
import com.github.wcordor.ledger.entity.IdempotencyKey;
import com.github.wcordor.ledger.entity.User;
import com.github.wcordor.ledger.exception.AccountDeletionFailureException;
import com.github.wcordor.ledger.exception.AccountNotFoundException;
import com.github.wcordor.ledger.exception.IdempotencyKeyAlreadyExistsException;
import com.github.wcordor.ledger.exception.UserNotFoundException;
import com.github.wcordor.ledger.mapper.AccountMapper;
import com.github.wcordor.ledger.repository.AccountRepository;
import com.github.wcordor.ledger.repository.IdempotencyKeyRepository;
import com.github.wcordor.ledger.repository.UserRepository;


@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final AccountMapper accountMapper;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository,
        IdempotencyKeyRepository idempotencyKeyRepository, AccountMapper accountMapper) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.accountMapper = accountMapper;
    }

    public AccountResponseDTO createAccount(String idempotencyKey, Long userId, AccountCreationDTO accountDTO) {
        IdempotencyKey savedKey = idempotencyKeyRepository.findByKey(idempotencyKey).orElse(null);

        if (savedKey != null) {
            if (savedKey.getExpiryDate().isBefore(LocalDateTime.now())) {
                idempotencyKeyRepository.delete(savedKey);
            } else {
                throw new IdempotencyKeyAlreadyExistsException();
            }
        }

        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Account account = accountRepository.save(
            new Account(user, accountDTO.name(), accountDTO.initialDeposit(),
            accountDTO.currency()));


        IdempotencyKey newKey = new IdempotencyKey(idempotencyKey, LocalDateTime.now().plusHours(24));
        idempotencyKeyRepository.save(newKey);
        
        return accountMapper.toDTO(account);
    }
    
    @SuppressWarnings("null")
    public List<String> getAccounts(Long userId) {
        return accountRepository.findByUser_Id(userId).stream().map(Account::getName).toList();
    }

    public AccountResponseDTO getAccount(Long accountId, Long userId) {
        Account account = accountRepository.findByIdAndUser_Id(accountId, userId)
            .orElseThrow(() -> new AccountNotFoundException(accountId, userId));

        return accountMapper.toDTO(account);
    }

    @Transactional
    public AccountResponseDTO changeName(String idempotencyKey, Long accountId, Long userId, AccountPatchDTO accountDTO) {
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

        if (accountDTO.getName().isPresent()) {
            account.setName(accountDTO.getName().get());
        }

        IdempotencyKey newKey = new IdempotencyKey(idempotencyKey, LocalDateTime.now().plusHours(24));
        idempotencyKeyRepository.save(newKey);
        
        return accountMapper.toDTO(account);        
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

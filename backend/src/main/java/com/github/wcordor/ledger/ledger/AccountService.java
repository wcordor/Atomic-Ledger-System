package com.github.wcordor.ledger.ledger;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.github.wcordor.ledger.AccountRepository;
import com.github.wcordor.ledger.User;
import com.github.wcordor.ledger.UserRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public Account createAccount(Long userId, String name, BigDecimal initialDeposit, String currency) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        Account account = new Account(user, name, initialDeposit, currency);
        accountRepository.save(account);
        account = accountRepository.findById(account.getId())
        .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        
        return account;
    }

}

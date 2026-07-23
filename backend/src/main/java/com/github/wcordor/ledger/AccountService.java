package com.github.wcordor.ledger;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AccountService {

    private final AccountRepo accountRepo;
    private final UserRepo userRepo;

    public AccountService(AccountRepo accountRepo, UserRepo userRepo) {
        this.accountRepo = accountRepo;
        this.userRepo = userRepo;
    }

    public Account createAccount(/*User user*/Long userId, String name, BigDecimal initialDeposit, String currency) {
        User user = userRepo.findById(userId).orElseThrow(() -> new EntityNotFoundException("User not found"));
        Account account = new Account(user, name, initialDeposit, currency);
        accountRepo.save(account);
        account = accountRepo.findWithTransactions(account.getId())
        .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        
        return account;
    }

}

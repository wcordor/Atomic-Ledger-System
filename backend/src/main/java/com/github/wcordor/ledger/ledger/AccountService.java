package com.github.wcordor.ledger.ledger;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.wcordor.ledger.AccountNotFoundException;
import com.github.wcordor.ledger.AccountRepository;
import com.github.wcordor.ledger.User;
import com.github.wcordor.ledger.UserNotFoundException;
import com.github.wcordor.ledger.UserRepository;


@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public Account createAccount(Long userId, String name, BigDecimal initialDeposit, String currency) {
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        Account account = new Account(user, name, initialDeposit, currency);
        
        return accountRepository.save(account);
    }
    
    public List<Account> getAccounts(Long userId) {
        return accountRepository.findByUser_Id(userId);
    }

    public Account getAccount(Long accountId, Long userId) {
        return accountRepository.findByIdAndUser_Id(accountId, userId).orElseThrow(() -> new AccountNotFoundException(accountId, userId));
    }

    @Transactional
    public Account changeName(Long accountId, Long userId, String name) {
        Account account = accountRepository.findWithLockingByIdAndUser_Id(accountId, userId)
            .orElseThrow(() -> new AccountNotFoundException(accountId, userId));

        account.setName(name);
        
        return accountRepository.save(account);        
    }

}

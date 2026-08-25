package com.github.wcordor.ledger.mapper;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Component;

import com.github.wcordor.ledger.dtos.accountDTO.AccountCreationDTO;
import com.github.wcordor.ledger.dtos.accountDTO.AccountResponseDTO;
import com.github.wcordor.ledger.entity.Account;
import com.github.wcordor.ledger.entity.Transaction;

@Component
public class AccountMapper {
    
    public AccountResponseDTO toDTO(Account account) {
        String name = account.getName();
        BigDecimal balance = account.getBalance();
        String currency = account.getCurrency();

        @SuppressWarnings("null")
        List<String> transactions = account.getTransactions().stream()
            .map(Transaction::getAmountAndCurrency).toList();

        String owner = account.getUserName();

        return new AccountResponseDTO(name, balance, currency, owner, transactions, id);
    }

    public Account toAccount(AccountCreationDTO accountDTO) {

        Long userId = accountDTO.userId();
        User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        return new Account(user, accountDTO.name(), accountDTO.initialDeposit(), accountDTO.currency());
    }
}

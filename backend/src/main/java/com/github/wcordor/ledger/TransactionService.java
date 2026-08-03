package com.github.wcordor.ledger;

import java.math.BigDecimal;

import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;


@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepo transactionRepo;

    public TransactionService(AccountRepository accountRepository, TransactionRepo transactionRepo) {
        this.accountRepository = accountRepository;
        this.transactionRepo = transactionRepo;
    }

    @Retryable(retryFor = { PessimisticLockingFailureException.class }, maxAttempts = 3,
         backoff = @Backoff(delay = 50, maxDelay = 150, multiplier = 2.0))
    @Transactional(rollbackFor = { InsufficientFundsException.class })
    public void transferMoney(Long receiverId, Long senderId, BigDecimal amount, String currency) 
    throws InsufficientFundsException {

        Account receiver = getAccountWithTransactionLists(receiverId);
        Account sender = getAccountWithTransactionLists(senderId);

        Transaction transaction = new Transaction((Account) null, (Account) null, null, null, null);

        transaction.setReceiver(receiver);
        transaction.setSender(sender);
        transaction.setAmount(amount);
        transaction.setCurrency(currency);
        transaction.setStatus(Status.PENDING);

        receiver.addTransaction(transaction);
        sender.addTransaction(transaction);

        BigDecimal expected_senderBal = sender.getBalance().subtract(amount);
        if (expected_senderBal.signum() == -1) {
            transaction.setStatus(Status.FAILED);
            transactionRepo.save(transaction);
            throw new InsufficientFundsException("Not enough funds to make transaction, canceling transaction.");
        }
        else {
            sender.debit(amount);
            receiver.credit(amount);

            accountRepository.save(receiver);
            accountRepository.save(sender);
            transaction.setStatus(Status.SUCCESSFUL);
            transactionRepo.save(transaction);
        }

    }

    public Account getAccountWithTransactionLists(Long id) {
        Account account = accountRepository.findWithLockingById(id)
        .orElseThrow(() -> new EntityNotFoundException("Account not found"));
        account.getSent().size();
        account.getReceived().size();
        return account;
    }
    
}

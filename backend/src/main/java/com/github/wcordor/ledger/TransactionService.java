package com.github.wcordor.ledger;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;


@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final IdempotencyKeyRepository idempotencyKeyRepository;
    private final AccountService accountService;

    public TransactionService(AccountRepository accountRepository, TransactionRepository transactionRepository,
        IdempotencyKeyRepository idempotencyKeyRepository, AccountService accountService) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.idempotencyKeyRepository = idempotencyKeyRepository;
        this.accountService = accountService;
    }

    @Retryable(retryFor = { PessimisticLockingFailureException.class }, maxAttempts = 3,
        backoff = @Backoff(delay = 50, maxDelay = 150, multiplier = 2.0))
    @Transactional(rollbackFor = { InsufficientFundsException.class })
    public Transaction moneyTransfer(String idempotencyKey, Long sender_userId, Long senderId, Long receiverId,
        BigDecimal amount, String currency) throws InsufficientFundsException {
        IdempotencyKey savedKey = idempotencyKeyRepository.findByKey(idempotencyKey).orElse(null);

        if (savedKey != null) {
            if (savedKey.getExpiryDate().isBefore(LocalDateTime.now())) {
                idempotencyKeyRepository.delete(savedKey);
            } else {
                throw new IdempotencyKeyAlreadyExistsException();
            }
        }
        
        Account sender = accountRepository.findWithLockingByIdAndUser_Id(senderId, sender_userId)
        .orElseThrow(() -> new AccountNotFoundException(senderId, sender_userId));

        Account receiver = accountRepository.findWithLockingById(receiverId)
            .orElseThrow(() -> new EntityNotFoundException("Account " + receiverId + " not found"));

        BigDecimal expected_senderBal = sender.getBalance().subtract(amount);
        
        if (expected_senderBal.signum() == -1) {
            throw new InsufficientFundsException();
        }

        sender.debit(amount);
        receiver.credit(amount);

        accountRepository.save(receiver);
        accountRepository.save(sender);

        Transaction transaction = new Transaction(receiver, sender, amount, currency);

        IdempotencyKey newKey = new IdempotencyKey(idempotencyKey, LocalDateTime.now().plusHours(24));
        idempotencyKeyRepository.save(newKey);

        return transactionRepository.save(transaction);
    }

    public List<Transaction> getTransactions(Long accountId, Long userId) {
        Account account = accountService.getAccount(accountId, userId);
        
        return account.getTransactions();
    }

    public Transaction getTransaction(Long transactionId, Long accountId, Long userId) {
        accountService.getAccount(accountId, userId);

        Transaction transaction = transactionRepository.findById(transactionId)
            .orElseThrow(() -> new TransactionNotFoundException(transactionId, accountId));

        if (transaction.getSenderId() != accountId && transaction.getReceiverId() != accountId) {
            throw new TransactionNotFoundException(transactionId, accountId);
        }

        return transaction;
    }
    
}

package com.github.wcordor.ledger;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
        
        Account sender = getAccountWithTransactions(senderId, sender_userId);

        Account receiver = accountRepository.findWithLockingById(receiverId)
            .orElseThrow(() -> new EntityNotFoundException("Account " + receiverId + " not found"));

        receiver = getAccountWithTransactions(receiverId, receiver.getUserId());

        BigDecimal expected_senderBal = sender.getBalance().subtract(amount);
        
        if (expected_senderBal.signum() == -1) {
            throw new InsufficientFundsException();
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

    public Account getAccountWithTransactions(Long accountId, Long userId) {
        Account account = accountRepository.findWithLockingByIdAndUser_Id(accountId, userId)
        .orElseThrow(() -> new AccountNotFoundException(accountId, userId));
        account.getSent().size();
        account.getReceived().size();
        return account;
    }
    
}

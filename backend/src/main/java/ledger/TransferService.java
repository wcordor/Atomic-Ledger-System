package ledger;

import java.math.BigDecimal;

import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityNotFoundException;


@Service
public class TransferService {

    private final AccountRepo accountRepo;
    private final TransactionRepo transactionRepo;

    public TransferService(AccountRepo accountRepo, TransactionRepo transactionRepo) {
        this.accountRepo = accountRepo;
        this.transactionRepo = transactionRepo;
    }

    @Retryable(retryFor = { ObjectOptimisticLockingFailureException.class }, maxAttempts = 3,
         backoff = @Backoff(delay = 50, maxDelay = 150, multiplier = 2.0))
    @Transactional(rollbackFor = { InsufficientFundsException.class })
    public void transferMoney(Long receiverId, Long senderId, BigDecimal amt, String currency/*, 
        Transaction transaction*/) throws InsufficientFundsException {

        Account receiver = getAccountWithTransactionLists(receiverId);
        Account sender = getAccountWithTransactionLists(senderId);

        Transaction transaction = new Transaction((Account) null, (Account) null, null, null, null);

        transaction.setReceiver(receiver);
        transaction.setSender(sender);
        transaction.setAmount(amt);
        transaction.setCurrency(currency);
        transaction.setStatus(Status.PENDING);

        receiver.addTransaction(transaction);
        sender.addTransaction(transaction);

        BigDecimal senderBal = sender.getBalance().subtract(amt);
        if (senderBal.signum() == -1) {
            transaction.setStatus(Status.FAILED);
            transactionRepo.save(transaction);
            throw new InsufficientFundsException("Not enough funds to make transaction, canceling transaction.");
        }
        else {
            BigDecimal receiverBal = receiver.getBalance().add(amt);
            
            // FIXME: debit() and credit() here
            withdraw(senderId, senderBal);
            deposit(receiverId, receiverBal);
            
            accountRepo.save(receiver);
            accountRepo.save(sender);
            transaction.setStatus(Status.SUCCESSFUL);
            transactionRepo.save(transaction);
        }

    }

    public Account getAccountWithTransactionLists(Long id) {
        Account account = accountRepo.findWithLockingById(id).orElseThrow(() -> new EntityNotFoundException("Account not found"));
        account.getSent().size();
        account.getReceived().size();
        return account;
    }
    
}

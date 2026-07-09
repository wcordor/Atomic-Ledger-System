package ledger;

import java.math.BigDecimal;

import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class TransferService {

    private final AccountRepo accountRepo;

    public TransferService(AccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    @Retryable(retryFor = { RuntimeException.class }, maxAttempts = 3)
    @Transactional(rollbackOn = { InsufficientFundsException.class })
    public void transferMoney(Long receiverId, Long senderId, BigDecimal amt, String currency, 
        Transaction transaction) throws InsufficientFundsException {

        Account receiver = accountRepo.findWithLockingById(receiverId).orElseThrow(() -> new RuntimeException("Receiver account not found"));
        Account sender = accountRepo.findWithLockingById(senderId).orElseThrow(() -> new RuntimeException("Sender account not found"));

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
            throw new InsufficientFundsException("Not enough funds to make transaction, canceling transaction.");
        }
        else {
            BigDecimal receiverBal = receiver.getBalance().add(amt);
            
            sender.setBalance(senderBal);
            receiver.setBalance(receiverBal);
            
            accountRepo.save(receiver);
            accountRepo.save(sender);
            transaction.setStatus(Status.SUCCESSFUL);
        }

    }
    
}

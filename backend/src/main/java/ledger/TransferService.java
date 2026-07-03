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

        Account receiver = accountRepo.findById(receiverId).orElseThrow(() -> new RuntimeException("Receiver account not found"));
        Account sender = accountRepo.findById(senderId).orElseThrow(() -> new RuntimeException("Sender account not found"));

        transaction.setReceiver(receiver);
        transaction.setSender(sender);
        transaction.setAmount(amt);
        transaction.setCurrency(currency);
        transaction.setStatus(Status.PENDING);

        receiver.addTransaction(transaction);
        sender.addTransaction(transaction);

        BigDecimal senderBal = accountRepo.findWithLockingById(senderId).get().getBalance().subtract(amt);
        if (senderBal.signum() == -1) {
            transaction.setStatus(Status.FAILED);
            throw new InsufficientFundsException("Not enough funds to make transaction, canceling transaction.");
        }
        else {
            BigDecimal receiverBal = accountRepo.findWithLockingById(receiverId).get().getBalance().add(amt);
            
            accountRepo.findWithLockingById(senderId).get().setBalance(senderBal);
            accountRepo.findWithLockingById(receiverId).get().setBalance(receiverBal);
            
            accountRepo.save(accountRepo.findWithLockingById(receiverId).get());
            accountRepo.save(accountRepo.findWithLockingById(senderId).get());
            transaction.setStatus(Status.SUCCESSFUL);
        }

    }
    
}

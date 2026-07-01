package clean;

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
    public void transferMoney(Long toId, Long fromId, BigDecimal amt) throws InsufficientFundsException {

        BigDecimal outgoingBal = accountRepo.findWithLockingById(fromId).get().getBalance().subtract(amt);
        if (outgoingBal.signum() == -1) {
            throw new InsufficientFundsException("Not enough funds to make transaction, canceling transaction.");
        }
        else {
            BigDecimal incomingBal = accountRepo.findWithLockingById(toId).get().getBalance().add(amt);
            
            accountRepo.findWithLockingById(fromId).get().setBalance(outgoingBal);
            accountRepo.findWithLockingById(toId).get().setBalance(incomingBal);
            
            accountRepo.save(accountRepo.findWithLockingById(toId).get());
            accountRepo.save(accountRepo.findWithLockingById(fromId).get());
        }

    }
    
}

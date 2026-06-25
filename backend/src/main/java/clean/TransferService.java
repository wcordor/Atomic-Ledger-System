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
    public void transferMoney(Long inId, Long outId, BigDecimal amt) throws InsufficientFundsException {

        BigDecimal outBalance = accountRepo.findWithLockingById(outId).get().getBalance().subtract(amt);
        if (outBalance.signum() == -1) {
            throw new InsufficientFundsException("Not enough funds to make transaction, canceling transaction.");
        }
        else {
            BigDecimal inBalance = accountRepo.findWithLockingById(inId).get().getBalance().add(amt);
            
            accountRepo.findWithLockingById(outId).get().setBalance(outBalance);
            accountRepo.findWithLockingById(inId).get().setBalance(inBalance);
            
            accountRepo.save(accountRepo.findWithLockingById(inId).get());
            accountRepo.save(accountRepo.findWithLockingById(outId).get());
        }

    }
    
}

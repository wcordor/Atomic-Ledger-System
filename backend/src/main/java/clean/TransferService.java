package clean;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class TransferService {

    private final AccountRepo accountRepo;

    public TransferService(AccountRepo accountRepo) {
        this.accountRepo = accountRepo;
    }

    @Transactional(rollbackOn = { InsufficientFundsException.class })
    public void transferMoney(Account in, Account out, BigDecimal amt) throws InsufficientFundsException {

        BigDecimal outBalance = out.getBalance().subtract(amt);
        if (outBalance.signum() == -1) {
            throw new InsufficientFundsException("Not enough funds to make transaction, canceling transaction.");
        }
        else {
            BigDecimal inBalance = in.getBalance().add(amt);
            
            out.setBalance(outBalance);
            in.setBalance(inBalance);
            
            accountRepo.save(in);
            accountRepo.save(out);
        }

    }
    
}

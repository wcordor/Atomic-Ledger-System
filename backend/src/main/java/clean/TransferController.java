package clean;

import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfer")
public class TransferController {

    private final TransferService transferService;
    private final AccountRepo accountRepo;

    public TransferController(TransferService transferService, AccountRepo accountRepo) {
        this.transferService = transferService;
        this.accountRepo = accountRepo;
    }

    @PostMapping
    public String handleTransfer(@RequestBody TransferRequest request) throws InsufficientFundsException {
        try {
            Account from = accountRepo.findById(request.getOutAccId()).orElseThrow(() -> new RuntimeException("Sender not found"));
            Account to = accountRepo.findById(request.getInAccId()).orElseThrow(() -> new RuntimeException("Receiver not found"));

            transferService.transferMoney(to.getId(), from.getId(), request.getAmt());
            return "Transfer processed successfully";
        } catch (InsufficientFundsException e) {
            return "ERROR: " + e.getMessage();
        } catch (PessimisticLockingFailureException e) {
            return "ERROR: Could not acquire lock on accounts. Please try again.";
        } catch (Exception e) {
            return "An unexpected error occurred.";
        }
    }

}

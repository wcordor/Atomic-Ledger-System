package clean;

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

            transferService.transferMoney(from, to, request.getAmt());
            return "Transfer processed successfully";
        } catch (InsufficientFundsException e) {
            return "ERROR: " + e.getMessage();
        } catch (Exception e) {
            return "An unexpected error occured.";
        }
    }

}

package ledger;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {

    private final TransactionRepo transactionRepo;
    private final TransactionModelAssembler assembler;

    public TransactionController(TransactionRepo transactionRepo, TransactionModelAssembler assembler) {
        this.transactionRepo = transactionRepo;
        this.assembler = assembler;
    }

    @GetMapping("/transactions")
    public CollectionModel<EntityModel<Transaction>> all() {

        List<EntityModel<Transaction>> transactions = transactionRepo.findAll().stream()
                .map(assembler::toModel)
                .collect(Collectors.toList());

        return CollectionModel.of(transactions,
                linkTo(methodOn(TransactionController.class).all()).withSelfRel());
    }

    @GetMapping("/transactions/{id}")
    public EntityModel<Transaction> one(@PathVariable Long id) {

        Transaction transaction = transactionRepo.findById(id)
            .orElseThrow(() -> new TransactionNotFoundException(id));

        return assembler.toModel(transaction);
    }

    @PostMapping("/transactions")
    public ResponseEntity<EntityModel<Transaction>> newTransaction(@RequestBody Transaction transaction) {
        
        Transaction newTransaction = transactionRepo.save(transaction);

        return ResponseEntity
                .created(linkTo(methodOn(TransactionController.class).one(newTransaction.getId())).toUri())
                .body(assembler.toModel(newTransaction));
    }
   
}

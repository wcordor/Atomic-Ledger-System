package ledger;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class TransactionModelAssembler implements RepresentationModelAssembler<Transaction, EntityModel<Transaction>> {
    
    @Override
    public EntityModel<Transaction> toModel(Transaction transaction) {

        EntityModel<Transaction> transactionModel = EntityModel.of(transaction,
            linkTo(methodOn(TransactionController.class).one(transaction.getId())).withSelfRel(),
            linkTo(methodOn(TransactionController.class).all()).withRel("transactions")
        );

        if (transaction.getStatus() == Status.PENDING) {
            transactionModel.add(
                linkTo(methodOn(TransactionController.class).complete(transaction.getId())).withRel("complete"),
                linkTo(methodOn(TransactionController.class).cancel(transaction.getId())).withRel("cancel")
            );
        }
        return EntityModel.of(transaction);
    }
}

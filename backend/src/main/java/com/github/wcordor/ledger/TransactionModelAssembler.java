package com.github.wcordor.ledger;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class TransactionModelAssembler implements RepresentationModelAssembler<Transaction, EntityModel<Transaction>> {
    
    @Override
    public EntityModel<Transaction> toModel(Transaction transaction) {

        EntityModel<Transaction> transactionModel = EntityModel.of(transaction);
        
        return transactionModel;
    }
}

package com.github.wcordor.ledger;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;


import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

@Component
public class TransactionModelAssembler implements RepresentationModelAssembler<Transaction, EntityModel<Transaction>> {
    
    @Override
    public EntityModel<Transaction> toModel(Transaction transaction) {

        EntityModel<Transaction> transactionModel = EntityModel.of(transaction,
            linkTo(methodOn(UserController.class).one(transaction.getId())).withSelfRel(),
            linkTo(methodOn(UserController.class).all()).withRel("transactions")
        );
        
        return transactionModel;
    }
}

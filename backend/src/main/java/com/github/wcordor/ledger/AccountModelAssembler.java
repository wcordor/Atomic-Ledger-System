package com.github.wcordor.ledger;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.github.wcordor.ledger.ledger.Account;

@Component
public class AccountModelAssembler implements RepresentationModelAssembler<Account, EntityModel<Account>> {

    @Override
    public EntityModel<Account> toModel(Account account) {

      EntityModel<Account> accountModel = EntityModel.of(account,
          linkTo(methodOn(UserController.class).oneAccount(account.getUserId(), account.getId())).withSelfRel(),
          linkTo(methodOn(UserController.class).allAccounts(account.getUserId())).withRel("accounts"));

      return accountModel;
  }
    
}

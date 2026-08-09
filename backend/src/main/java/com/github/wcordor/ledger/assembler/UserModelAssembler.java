package com.github.wcordor.ledger.assembler;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.RepresentationModelAssembler;
import org.springframework.stereotype.Component;

import com.github.wcordor.ledger.controller.UserController;
import com.github.wcordor.ledger.entity.User;

@Component
public class UserModelAssembler implements RepresentationModelAssembler<User, EntityModel<User>> {

    @Override
    public EntityModel<User> toModel(User user) {

      EntityModel<User> userModel = EntityModel.of(user,
          linkTo(methodOn(UserController.class).one(user.getId())).withSelfRel(),
          linkTo(methodOn(UserController.class).all()).withRel("users"));

      return userModel;
  }
    
}

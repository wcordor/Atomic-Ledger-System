package com.github.wcordor.ledger;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.github.wcordor.ledger.ledger.Account;
import com.github.wcordor.ledger.ledger.AccountService;

@RestController
public class UserController {

    private final UserModelAssembler assembler;
	private final AccountModelAssembler accountAssembler;

	private final UserService service;
	private final AccountService accountService;

    public UserController(UserService service, AccountService accountService, UserModelAssembler assembler, 
		AccountModelAssembler accountAssembler) {

		this.assembler = assembler;
		this.accountAssembler = accountAssembler;
		this.service = service;
		this.accountService = accountService;
	}

    @GetMapping("/users")
	public CollectionModel<EntityModel<User>> all() {

		List<EntityModel<User>> users = service.getAll().stream()
		.map(assembler::toModel).collect(Collectors.toList());

		return CollectionModel.of(users, linkTo(methodOn(UserController.class).all()).withSelfRel());
	}

    @PostMapping("/users")
	public ResponseEntity<?> newUser(@RequestBody User newUser) {

		EntityModel<User> entityModel = assembler.toModel(service.saveUser(newUser));

		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	@GetMapping("/users/{id}")
	public EntityModel<User> one(@PathVariable("id") Long id) {

		User user = service.getUser(id);
		
		return assembler.toModel(user);
	}

	@PutMapping("/users/{id}")
	public ResponseEntity<?> replaceUser(@RequestBody User newUser, @PathVariable Long id) {

		User updatedUser = service.changeName(id, userRequest.getFirstName(), userRequest.getLastName());		
		EntityModel<User> entityModel = assembler.toModel(updatedUser);

		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	@DeleteMapping("/users/{id}")
	public ResponseEntity<?> deleteUser(@PathVariable Long id) {

		service.deleteUser(id);

		return ResponseEntity.noContent().build();
	}
    
}

package com.github.wcordor.ledger;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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

		EntityModel<User> entityModel = assembler.toModel(service.createUser(newUser.getFirstName(), newUser.getLastName()));

		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	@GetMapping("/users/{id}")
	public EntityModel<User> one(@PathVariable("id") Long id) {

		User user = service.getUser(id);
		
		return assembler.toModel(user);
	}

	@PutMapping("/users/{id}")
	public ResponseEntity<?> replaceUser(@PathVariable Long id, @RequestBody User userRequest) {

		User updatedUser = service.changeName(id, userRequest.getFirstName(), userRequest.getLastName());		
		EntityModel<User> entityModel = assembler.toModel(updatedUser);
		
		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	@PatchMapping("/users/{id}")
	public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody Map<String, Object> updates) {
		
		User user = service.updateUser(id, updates);
		EntityModel<User> entityModel = assembler.toModel(user);

		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}
	

	@DeleteMapping("/users/{id}/remove")
	public ResponseEntity<?> deleteUser(@PathVariable Long id) {

		service.deleteUser(id);

		return ResponseEntity.noContent().build();
	}

	@GetMapping("/users/{id}/accounts")
	public CollectionModel<EntityModel<Account>> allAccounts(@PathVariable("id") Long userId) {

		List<EntityModel<Account>> accounts = accountService.getAccounts(userId).stream()
		.map(accountAssembler::toModel).collect(Collectors.toList());

		return CollectionModel.of(accounts, linkTo(methodOn(UserController.class).allAccounts(userId)).withSelfRel());
	}  

	@GetMapping("/users/{id}/accounts/{accountId}")
	public EntityModel<Account> oneAccount(@PathVariable("id") Long userId, @PathVariable("accountId") Long accountId) {

		Account account = accountService.getAccount(accountId, userId);

		return accountAssembler.toModel(account);
	}
	
	@PostMapping("users/{id}/accounts")
	public ResponseEntity<?> newAccount(@PathVariable("id") Long userId, @RequestBody Account newAccount) {

		EntityModel<Account> entityModel = accountAssembler.toModel(accountService.createAccount(userId, newAccount.getName(),
			newAccount.getBalance(), newAccount.getCurrency()));

		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	@PatchMapping("users/{id}/accounts/{accountId}")
	public ResponseEntity<?> changeAccountName(@PathVariable("id") Long userId, @PathVariable("accountId") Long accountId,
	@RequestBody Map<String, String> nameChange) {
		Account account = accountService.changeName(accountId, userId, nameChange.get("name"));
		EntityModel<Account> entityModel = accountAssembler.toModel(account);

		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}
    
}

package com.github.wcordor.ledger;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    private final UserModelAssembler assembler;
	private final AccountModelAssembler accountAssembler;
	private final TransactionModelAssembler transactionAssembler;

	private final UserService service;
	private final AccountService accountService;
	private final TransactionService transactionService;

    public UserController(UserService service, AccountService accountService, TransactionService transactionService,
		UserModelAssembler assembler, AccountModelAssembler accountAssembler, TransactionModelAssembler transactionAssembler) {

		this.assembler = assembler;
		this.accountAssembler = accountAssembler;
		this.transactionAssembler = transactionAssembler;
		this.service = service;
		this.accountService = accountService;
		this.transactionService = transactionService;
	}

    @GetMapping("/users")
	public CollectionModel<EntityModel<User>> all() {

		List<EntityModel<User>> users = service.getAll().stream()
		.map(assembler::toModel).collect(Collectors.toList());

		return CollectionModel.of(users, linkTo(methodOn(UserController.class).all()).withSelfRel());
	}

    @PostMapping("/users")
	public ResponseEntity<?> newUser(@RequestHeader("Idempotency-Key") String idempotencyKey, @RequestBody User newUser) {

		EntityModel<User> entityModel = assembler.toModel(service.createUser(idempotencyKey, newUser.getFirstName(), newUser.getLastName()));

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
	public ResponseEntity<?> updateUser(@RequestHeader("Idempotency-Key") String idempotencyKey,
		@PathVariable Long id, @RequestBody Map<String, Object> updates) {
		
		User user = service.updateUser(idempotencyKey, id, updates);
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
	public ResponseEntity<?> newAccount(@RequestHeader("Idempotency-Key") String idempotencyKey,
		@PathVariable("id") Long userId, @RequestBody Account newAccount) {

		EntityModel<Account> entityModel = accountAssembler.toModel(accountService.createAccount(idempotencyKey, userId, newAccount.getName(),
			newAccount.getBalance(), newAccount.getCurrency()));

		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	@PatchMapping("users/{id}/accounts/{accountId}")
	public ResponseEntity<?> changeAccountName(@RequestHeader("Idempotency-Key") String idempotencyKey,
		@PathVariable("id") Long userId, @PathVariable("accountId") Long accountId, @RequestBody Map<String, String> nameChange) {

		Account account = accountService.changeName(idempotencyKey, accountId, userId, nameChange.get("name"));
		EntityModel<Account> entityModel = accountAssembler.toModel(account);

		return ResponseEntity.created(entityModel.getRequiredLink(IanaLinkRelations.SELF).toUri()).body(entityModel);
	}

	@DeleteMapping("users/{id}/accounts/{accountId}/remove")
	public ResponseEntity<?> deleteAccount(@PathVariable("id") Long userId, @PathVariable("accountId") Long accountId) {

		accountService.deleteAccount(accountId, userId);

		return ResponseEntity.noContent().build();
	}

	@PostMapping("users/{id}/accounts/{accountId}/money-transfer")
	public ResponseEntity<?> newTransaction(@RequestHeader("Idempotency-Key") String idempotencyKey,
		@PathVariable("id") Long userId, @PathVariable("accountId") Long accountId, @RequestBody Transaction newTransaction) {

		if (accountId != newTransaction.getSenderId()) {
			return ResponseEntity.badRequest().body("Account ID in path does not match sender ID in request body.");
		}

		EntityModel<Transaction> entityModel = transactionAssembler.toModel(transactionService.moneyTransfer(idempotencyKey, userId,
		accountId, newTransaction.getReceiverId(), newTransaction.getAmount(), newTransaction.getCurrency()));

		return ResponseEntity.status(HttpStatus.CREATED).body(entityModel);
	}

	@GetMapping("users/{id}/accounts/{accountId}/transactions")
	public CollectionModel<EntityModel<Transaction>> allTransactions(@PathVariable("id") Long userId,
		@PathVariable("accountId") Long accountId) {
		
		List<EntityModel<Transaction>> transactions = transactionService.getTransactions(accountId, userId).stream()
		.map(transactionAssembler::toModel).collect(Collectors.toList());

		return CollectionModel.of(transactions, linkTo(methodOn(UserController.class).allTransactions(userId, accountId)).withSelfRel());
	}

	@GetMapping("users/{id}/accounts/{accountId}/transactions/{transactionId}")
    public EntityModel<Transaction> oneTransaction(@PathVariable("id") Long userId,
		@PathVariable("accountId") Long accountId, @PathVariable("transactionId") Long transactionId) {
		
		Transaction transaction = transactionService.getTransaction(transactionId, accountId, userId);

		return transactionAssembler.toModel(transaction);
	}
}

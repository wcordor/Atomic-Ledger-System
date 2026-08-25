package com.github.wcordor.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.github.wcordor.ledger.dtos.accountDTO.*;
import com.github.wcordor.ledger.dtos.transactionDTO.*;
import com.github.wcordor.ledger.dtos.userDTO.*;
import com.github.wcordor.ledger.entity.Account;
import com.github.wcordor.ledger.entity.Transaction;
import com.github.wcordor.ledger.entity.User;
import com.github.wcordor.ledger.exception.*;
import com.github.wcordor.ledger.repository.*;
import com.github.wcordor.ledger.service.*;

import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest
class LedgerApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(LedgerApplicationTests.class);

	@Autowired
	private AccountRepository accountRepository;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private TransactionRepository transactionRepository;

	@Autowired
	private IdempotencyKeyRepository idempotencyKeyRepository;

	@Autowired
	private AccountService accountService;

	@Autowired
	private UserService userService;

    @Autowired
    DemoRequestFactory requestFactory;

    private AccountResponseDTO accountDTO;
    private Account account;
    private Long account_id;
    private AccountResponseDTO accountDTO2;
    private Account account2;
    private Long account2_id;

    private UserResponseDTO userDTO;
    private User user;
    private Long user_id;
    private UserResponseDTO userDTO2;
    private User user2;
    private Long user2_id;

	@BeforeEach
	void setUp() {

		accountRepository.deleteAll();
		userRepository.deleteAll();
		transactionRepository.deleteAll();
		idempotencyKeyRepository.deleteAll();

        userDTO = requestFactory.createDemoUser("Account", "Owner I");
        user_id = userDTO.id();

        accountDTO = requestFactory.createDemoAccount(user_id, "Account I",
            new BigDecimal("1000.00"), "USD");

        account_id = accountDTO.id();

        userDTO = userService.getUser(user_id);
       
        userDTO2 = requestFactory.createDemoUser("Account", "Owner II");
        user2_id = userDTO2.id();

        accountDTO2 = requestFactory.createDemoAccount(user2_id, "Account II", new BigDecimal("200.00"), "USD");
        account2_id = accountDTO2.id();

        userDTO2 = userService.getUser(user2_id);

        account = requestFactory.getDemoAccount(account_id, user_id);
        account2 = requestFactory.getDemoAccount(account2_id, user2_id);
        user = requestFactory.getDemoUser(user_id);
        user2 = requestFactory.getDemoUser(user2_id);

	}

	@Test
	void testAccountFunctions() {

		assertNotNull(account_id);
		assertEquals("Account I", account.getName());
		assertEquals(new BigDecimal("1000.00"), account.getBalance());
		assertEquals("USD", account.getCurrency());
		assertEquals(user_id, account.getUserId());

		account.setName("Checking");
		assertEquals("Checking", account.getName());

		account.setCurrency("GBP");
		assertEquals("GBP", account.getCurrency());
		account.setUser(user2);
		assertNotEquals(user2_id, account.getUserId());
		assertEquals(user_id, account.getUserId());

		account.debit(new BigDecimal("100.00"));
		assertEquals(new BigDecimal("900.00"), account.getBalance());

		account.credit(new BigDecimal("200.00"));
		assertEquals(new BigDecimal("1100.00"), account.getBalance());

	}

	@Test
	void testAccountRepositoryFunctions() {

		assertEquals(1, accountRepository.findByName("Account I").size());
		assertTrue(accountRepository.findByName("Account I").contains(account));
		
		assertEquals(account,
			accountRepository.findById(account_id).orElseThrow(() -> new EntityNotFoundException("Account not found")));
		assertEquals(1, accountRepository.findByUserLastName("Owner I").size());
		assertTrue(accountRepository.findByUserLastName("Owner I").contains(account));

		List<Account> usd = accountRepository.findByCurrency("USD");
		assertEquals(2, usd.size());
		assertTrue(usd.contains(account) && usd.contains(account2));

		List<Account> userAccounts = accountRepository.findByUser_Id(user_id);
		assertEquals(1, userAccounts.size());
		assertTrue(userAccounts.contains(account));

		Account actual = accountRepository.findByIdAndUser_Id(account_id, user_id)
			.orElseThrow(() -> new AccountNotFoundException(account_id, user_id));

		assertEquals(account, actual);
			
	}

	@Test
	void testAccountServiceFunctions() {

		AccountResponseDTO accountDTO3 = accountService.createAccount("Key-Test", user_id,
            new AccountCreationDTO("Account III", new BigDecimal("300.00"), "USD", user_id));

		Long account3_id = accountDTO3.id();
		Account account3 = requestFactory.getDemoAccount(account3_id, user_id);

		List<String> accountList = accountService.getAccounts(user_id);
		assertEquals(2, accountList.size());
		assertTrue(accountList.contains(account.getInfo()) && accountList.contains(account3.getInfo()));

		assertThrows(IdempotencyKeyAlreadyExistsException.class, () -> {
			accountService.createAccount("Key-Test", user_id, 
            new AccountCreationDTO("invalid", new BigDecimal("300.00"), "USD", user_id));
		});

		AccountResponseDTO getAccount = accountService.getAccount(account3_id, user_id);
		assertEquals(accountDTO3, getAccount);

		accountDTO3 = accountService.changeName(UUID.randomUUID().toString(),
			account3_id, user_id, new AccountPatchDTO(JsonNullable.of("Savings")));

		assertEquals("Savings", accountDTO3.name());

		assertThrows(AccountDeletionFailureException.class, () -> {
			accountService.deleteAccount(account3_id, user_id);
		});

		transactionService.moneyTransfer(UUID.randomUUID().toString(), user_id, account3_id,
			new TransactionCreationDTO(account_id, new BigDecimal("300.00"), "USD"));
		
		accountService.deleteAccount(account3_id, user_id);

		accountList = accountService.getAccounts(user_id);	
		account = requestFactory.getDemoAccount(account_id, user_id);

		assertTrue(accountList.size() == 1 && accountList.contains(account.getInfo()));

		assertThrows(AccountNotFoundException.class, () -> {
			accountService.getAccount(account3.getId(), user2_id);
		});
	}

	@Test
	void testUserFunctions() {

		assertNotNull(user_id);
		assertEquals("Account", user.getFirstName());
		assertEquals("Owner I", user.getLastName());
		assertNotNull(user2_id);
		assertEquals("Account", user2.getFirstName());
		assertEquals("Owner II", user2.getLastName());

		assertEquals(1, user.getAccounts().size());
		assertTrue(user.getAccounts().contains(account));
		
		user.setFirstName("User");
		user.setLastName("1");
		String name = user.getFirstName() + " " + user.getLastName();
		assertEquals("User 1", name);
		user.addAccount(account2);
		List<Account> userAccs = user.getAccounts();

		assertFalse(2 == userAccs.size());

		AccountResponseDTO account3DTO = requestFactory.createDemoAccount(user_id, "Account III", new BigDecimal("5000.00"), "USD");
		Account account3 = requestFactory.getDemoAccount(account3DTO.id(), user_id);
		user = requestFactory.getDemoUser(user_id);
		userAccs = user.getAccounts();

		assertTrue(2 == userAccs.size());
		assertTrue(userAccs.contains(account) && userAccs.contains(acc3));
		
	}

	@Test
	void testUserRepositoryFunctions() {

		assertEquals(1, userRepository.findByLastName("Owner I").size());
		assertEquals(user, userRepository.findById(user_id)
			.orElseThrow(() -> new EntityNotFoundException("User not found")));
		assertEquals(1, userRepository.findByLastName("Owner II").size());
		assertEquals(user2, userRepository.findById(user2_id)
			.orElseThrow(() -> new EntityNotFoundException("User not found")));
	}

	@Test
	void testUserServiceFunctions() {

		userDTO = userService.changeName(user_id, new UserCreationDTO("Owner", "of Account I"));
		assertEquals("Owner", userDTO.firstName());
		assertEquals("of Account I", userDTO.lastName());
		assertThrows(UserDeletionFailureException.class, () -> {
			userService.deleteUser(user_id);
		});

		UserResponseDTO delete = userService.createUser(UUID.randomUUID().toString(), 
			new UserCreationDTO("To", "Delete"));

		List<User> userList = userService.getAll();
		assertEquals(3, userList.size());

		userService.deleteUser(delete.id());

		assertThrows(UserNotFoundException.class, () -> {
			userService.getUser(delete.id());
		});

		userList = userService.getAll();
		assertEquals(2, userList.size());

		userDTO = userService.updateUser(UUID.randomUUID().toString(), user_id,
			new UserPatchDTO(JsonNullable.undefined(), JsonNullable.of("PATCH")));

		assertEquals("Owner", userDTO.firstName());
		assertEquals("PATCH", userDTO.lastName());

		assertThrows(NullUserNameException.class, () -> {
			userService.changeName(user_id, new UserCreationDTO(null, null));
		});
	}

	@Test
	void testMoneyTransfers() {
		// acc balance: $1,000, acc2 balance: $200
		TransactionResponseDTO transactionDTO = 
			transactionService.moneyTransfer(UUID.randomUUID().toString(), user_id, account_id,
			new TransactionCreationDTO(account2_id, new BigDecimal("400.00"), "USD"));

		accountDTO = accountService.getAccount(account_id, user_id);
		accountDTO2 = accountService.getAccount(account2_id, user2_id);

		assertEquals(new BigDecimal("600.00"), accountDTO2.balance());
		assertEquals(new BigDecimal("600.00"), accountDTO.balance());
		
		assertThrows(InsufficientFundsException.class, () -> {
			transactionService.moneyTransfer(UUID.randomUUID().toString(), user_id, account_id,
				new TransactionCreationDTO(account2_id, new BigDecimal("4000.00"), "USD"));
		});

		try {
			transactionService.moneyTransfer(UUID.randomUUID().toString(), user_id, account_id,
				new TransactionCreationDTO(account2_id, new BigDecimal("800.00"), "USD"));
		} catch (InsufficientFundsException e) {
			logger.error("ERROR: " + e.getMessage());
		}

        accountDTO = accountService.getAccount(account_id, user_id);
		accountDTO2 = accountService.getAccount(account2_id, user2_id);

		assertEquals(new BigDecimal("600.00"), accountDTO2.balance());
		assertEquals(new BigDecimal("600.00"), accountDTO.balance());

		account = requestFactory.getDemoAccount(account_id, user_id);
        account2 = requestFactory.getDemoAccount(account2_id, user2_id);

        List<Transaction> a_transactions = account.getTransactions();
        List<Transaction> a2_transactions = account2.getTransactions();

		Transaction transaction = requestFactory.getDemoTransaction(transactionDTO.id(), account2_id, user2_id);

        assertEquals(1, a_transactions.size());
        assertEquals(1, a2_transactions.size());
		assertTrue(a_transactions.contains(transaction) && a2_transactions.contains(transaction));

		assertThrows(InvalidTransferException.class, () -> {
			transactionService.moneyTransfer(UUID.randomUUID().toString(), user_id, account_id,
				new TransactionCreationDTO(account_id, new BigDecimal("80.00"), "USD"));
		});
	
	}

	@Test
	void testTransactionFunctions() {

		TransactionResponseDTO transactionDTO = requestFactory.demoMoneyTransfer(user_id, account_id, account2_id,
			new BigDecimal("400.00"), "USD");
		
		Transaction transaction = requestFactory.getDemoTransaction(transactionDTO.id(), account_id, user_id);

		assertNotNull(transaction.getId());
		assertEquals(account_id, transaction.getSenderId());
		assertEquals(account2_id, transaction.getReceiverId());
		assertEquals(new BigDecimal("400.00"), transaction.getAmount());
		assertEquals("USD", transaction.getCurrency());

		List<Long> transactionAccIds = transaction.getAccountIds();
		assertEquals(2, transactionAccIds.size());
		assertTrue(transactionAccIds.contains(account_id) && transactionAccIds.contains(account2_id));
	}

	@Test
	void testTransactionServiceFunctions() {

		TransactionResponseDTO transactionDTO = 
			transactionService.moneyTransfer(UUID.randomUUID().toString(), user_id, account_id, 
			new TransactionCreationDTO(account2_id, new BigDecimal("400.00"), "USD"));
		
		List<Transaction> transactions = transactionService.getTransactions(account_id, user_id);
		assertEquals(1, transactions.size());

		Transaction transaction = requestFactory.getDemoTransaction(transactionDTO.id(), account_id, user_id);
		assertTrue(transactions.contains(transaction.getAmountAndCurrency()));

		TransactionResponseDTO actual = transactionService.getTransaction(transactionDTO.id(), account_id, user_id);
		long diffMillis = Math.abs(
			Duration.between(transactionDTO.timestamp(), actual.timestamp()).toMillis()
		);
		//assertEquals(transactionDTO, actual);
		assertEquals(transactionDTO.id(), actual.id());
		assertEquals(transactionDTO.senderId(), actual.senderId());
		assertEquals(transactionDTO.receiverId(), actual.receiverId());
		assertEquals(transactionDTO.amount(), actual.amount());
		assertEquals(transactionDTO.currency(), actual.currency());
		assertTrue(diffMillis < 1);

		assertThrows(TransactionNotFoundException.class, () -> {
			transactionService.getTransaction(4L, account_id, user_id); 
		});
	}

	@Test
	void testConcurrencySufficient() {

		List<CompletableFuture<Void>> futures = new ArrayList<>();

		// generate 50 CompletableFutures
		for (int i = 0; i < 50; i++) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				try {
					transactionService.moneyTransfer(UUID.randomUUID().toString(), user_id, account_id,
						new TransactionCreationDTO(account2_id, new BigDecimal("10.00"), "USD"));
				} catch (InsufficientFundsException e) {
					logger.error("ERROR: " + e.getMessage());
				}
			});
			futures.add(future);
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		accountRepository.flush();

		accountDTO = accountService.getAccount(account_id, user_id);
		accountDTO2 = accountService.getAccount(account2_id, user2_id);

		assertEquals(new BigDecimal("500.00"), accountDTO.balance());
		assertEquals(new BigDecimal("700.00"), accountDTO2.balance());

	}
	
	@Test
	void testConcurrencyInsufficient() {

		List<CompletableFuture<Void>> futures = new ArrayList<>();

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failCount = new AtomicInteger(0);

		for (int i = 0; i < 50; i++) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				try {
					transactionService.moneyTransfer(UUID.randomUUID().toString(), user_id, account_id, 
						new TransactionCreationDTO(account2_id, new BigDecimal("30.00"), "USD"));
						
					successCount.incrementAndGet();
				} catch (InsufficientFundsException e) {
					logger.error("ERROR: " + e.getMessage());
					failCount.incrementAndGet();
				}
			});
			futures.add(future);
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		accountRepository.flush();

		accountDTO = accountService.getAccount(account_id, user_id);
		accountDTO2 = accountService.getAccount(account2_id, user2_id);

		assertEquals(new BigDecimal("10.00"), accountDTO.balance());
		assertEquals(new BigDecimal("1190.00"), accountDTO2.balance());
		assertEquals(33, successCount.get());
		assertEquals(17, failCount.get());

	}
}

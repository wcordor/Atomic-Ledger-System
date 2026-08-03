package com.github.wcordor.ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
	private TransactionRepo tr;

	@Autowired
	private AccountService as;

	private Account acc;
	private Long acc_Id;
	private Account acc2;
	private Long acc2_Id;

	private User user;
	private Long user_Id;
	private User user2;
	private Long user2_Id;

	@BeforeEach
	void setUp() {

		accountRepository.deleteAll();
		userRepository.deleteAll();
		tr.deleteAll();

		user = new User("Account", "Owner");
		userRepository.save(user);
		user_Id = user.getId();

		acc = as.createAccount(user_Id, "Savings", new BigDecimal("1000.00"), "USD");
		user = userRepository.findById(user_Id)
			.orElseThrow(() -> new EntityNotFoundException("Account not found"));
		acc_Id = acc.getId();
		

		user2 = new User("Account", "Owner II");
		userRepository.save(user2);
		user2_Id = user2.getId();

		acc2 = as.createAccount(user2_Id, "Checking", new BigDecimal("200.00"), "USD");
		acc2_Id = acc2.getId();

	}

	@Test
	void testAccountFunctions() {

		assertNotNull(acc_Id);
		assertEquals("Savings", acc.getName());
		assertEquals(new BigDecimal("1000.00"), acc.getBalance());
		assertEquals("USD", acc.getCurrency());
		assertEquals(user_Id, acc.getUserId());

		acc.setName("Checking");
		assertEquals("Checking", acc.getName());

		acc.setCurrency("GBP");
		assertEquals("GBP", acc.getCurrency());
		acc.setUser(user2);
		assertNotEquals(user2_Id, acc.getUserId());
		assertEquals(user_Id, acc.getUserId());

	}

	@Test
	void testAccountRepoFunctions() {

		assertEquals(1, accountRepository.findByName("Savings").size());
		assertTrue(accountRepository.findByName("Savings").contains(acc));
		
		assertEquals(acc,
			accountRepository.findById(acc_Id).orElseThrow(() -> new EntityNotFoundException("Account not found")));
		assertEquals(1, accountRepository.findByUserLastName("Owner").size());
		assertTrue(accountRepository.findByUserLastName("Owner").contains(acc));

		List<Account> usd = accountRepository.findByCurrency("USD");
		assertEquals(2, usd.size());
		assertTrue(usd.contains(acc) && usd.contains(acc2));
		
	}

	@Test
	void testUserFunctions() {

		assertNotNull(user_Id);
		assertEquals("Account", user.getFirstName());
		assertEquals("Owner", user.getLastName());
		assertNotNull(user2_Id);
		assertEquals("Account", user2.getFirstName());
		assertEquals("Owner II", user2.getLastName());

		assertEquals(1, user.getAccounts().size());
		assertTrue(user.getAccounts().contains(acc));
		
		user.setFirstName("User");
		user.setLastName("1");
		String name = user.getFirstName() + " " + user.getLastName();
		assertEquals("User 1", name);
		user.addAccount(acc2);
		List<Account> userAccs = user.getAccounts();

		assertFalse(2 == userAccs.size());

		Account acc3 = as.createAccount(user_Id, "Investment", new BigDecimal("5000.00"), "USD");
		user = userRepository.findById(user_Id)
			.orElseThrow(() -> new EntityNotFoundException("User not found"));
		userAccs = user.getAccounts();

		assertTrue(2 == userAccs.size());
		assertTrue(userAccs.contains(acc) && userAccs.contains(acc3));
		
	}

	@Test
	void testUserRepoFunctions() {

		assertEquals(1, userRepository.findByLastName("Owner").size());
		assertEquals(user, userRepository.findById(user_Id)
			.orElseThrow(() -> new EntityNotFoundException("User not found")));
		assertEquals(1, userRepository.findByLastName("Owner II").size());
		assertEquals(user2, userRepository.findById(user2_Id)
			.orElseThrow(() -> new EntityNotFoundException("User not found")));
	}

	@Test
	void testMoneyTransfers() {
		// acc balance: $1,000, acc2 balance: $200
		
		try {
			transactionService.transferMoney(acc2_Id, acc_Id, new BigDecimal("400.00"), "USD");
		} catch (InsufficientFundsException e) {
			logger.error("ERROR: " + e.getMessage());
		}

		acc = accountRepository.findById(acc_Id).orElseThrow(() -> new EntityNotFoundException("Account not found"));
		acc2 = accountRepository.findById(acc2_Id).orElseThrow(() -> new EntityNotFoundException("Account not found"));

		assertEquals(new BigDecimal("600.00"), acc2.getBalance());
		assertEquals(new BigDecimal("600.00"), acc.getBalance());
		
		assertThrows(InsufficientFundsException.class, () -> {
			transactionService.transferMoney(acc2_Id, acc_Id, new BigDecimal("4000.00"), "USD");
		});

		try {
			transactionService.transferMoney(acc2_Id, acc_Id, new BigDecimal("800.00"), "USD");
		} catch (InsufficientFundsException e) {
			logger.error("ERROR: " + e.getMessage());
		}

        acc = accountRepository.findById(acc_Id).orElseThrow(() -> new EntityNotFoundException("Account not found"));
        acc2 = accountRepository.findById(acc2_Id).orElseThrow(() -> new EntityNotFoundException("Account not found"));

		assertEquals(new BigDecimal("600.00"), acc.getBalance());
		assertEquals(new BigDecimal("600.00"), acc2.getBalance());

        List<Transaction> a_transactions = acc.getTransactions();
        List<Transaction> a2_transactions = acc2.getTransactions();

		Transaction transaction = a_transactions.get(0);
        assertEquals(1, a_transactions.size());
        assertEquals(1, a2_transactions.size());
		assertTrue(a_transactions.contains(transaction) && a2_transactions.contains(transaction));
	
	}

	@Test
	void testTransactionFunctions() {

		try {
			transactionService.transferMoney(acc2_Id, acc_Id, new BigDecimal("400.00"), "USD");
		} catch (InsufficientFundsException e) {
			logger.error("ERROR: " + e.getMessage());
		}

		acc = accountRepository.findById(acc_Id).orElseThrow(() -> new EntityNotFoundException("Account not found"));
        acc2 = accountRepository.findById(acc2_Id).orElseThrow(() -> new EntityNotFoundException("Account not found"));

		List<Transaction> a_transactions = acc.getTransactions();
		

		Transaction transaction = a_transactions.get(0);
		assertNotNull(transaction.getId());
		assertEquals(acc_Id, transaction.getSenderId());
		assertEquals(acc2_Id, transaction.getReceiverId());
		transaction.setReceiver(acc);
		transaction.setSender(acc2);
		assertFalse(acc_Id.equals(transaction.getReceiverId()));
		assertFalse(acc2_Id.equals(transaction.getSenderId()));
		assertTrue(acc2_Id.equals(transaction.getReceiverId()));
		assertTrue(acc_Id.equals(transaction.getSenderId()));
		assertEquals(new BigDecimal("400.00"), transaction.getAmount());
		transaction.setAmount(new BigDecimal("9000000.00"));
		assertEquals(new BigDecimal("400.00"), transaction.getAmount());
		assertEquals("USD", transaction.getCurrency());
		transaction.setCurrency("GBP");
		assertEquals("USD", transaction.getCurrency());
		assertEquals(Status.SUCCESSFUL, transaction.getStatus());
		transaction.setStatus(Status.FAILED);
		assertEquals(Status.SUCCESSFUL, transaction.getStatus());

		List<Long> transactionAccIds = transaction.getAccountIds();
		assertEquals(2, transactionAccIds.size());
		assertTrue(transactionAccIds.contains(acc_Id) && transactionAccIds.contains(acc2_Id));

	}

	@Test
	void testConcurrencySufficient() {

		List<CompletableFuture<Void>> futures = new ArrayList<>();

		// generate 50 CompletableFutures
		for (int i = 0; i < 50; i++) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				try {
					transactionService.transferMoney(acc2_Id, acc_Id, new BigDecimal("10.00"), "USD");
				} catch (InsufficientFundsException e) {
					logger.error("ERROR: " + e.getMessage());
				}
			});
			futures.add(future);
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		accountRepository.flush();

		acc = accountRepository.findById(acc_Id).orElseThrow(() -> new EntityNotFoundException("Account not found"));
		acc2 = accountRepository.findById(acc2_Id).orElseThrow(() -> new EntityNotFoundException("Account not found"));

		assertEquals(new BigDecimal("500.00"), acc.getBalance());
		assertEquals(new BigDecimal("700.00"), acc2.getBalance());

	}
	
	@Test
	void testConcurrencyInsufficient() {

		List<CompletableFuture<Void>> futures = new ArrayList<>();

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failCount = new AtomicInteger(0);

		for (int i = 0; i < 50; i++) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				try {
					transactionService.transferMoney(acc2_Id, acc_Id, new BigDecimal("30.00"), "USD");
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

		acc = accountRepository.findById(acc_Id).orElseThrow(() -> new EntityNotFoundException("Account not found"));
		acc2 = accountRepository.findById(acc2_Id).orElseThrow(() -> new EntityNotFoundException("Account not found"));

		assertEquals(new BigDecimal("10.00"), acc.getBalance());
		assertEquals(new BigDecimal("1190.00"), acc2.getBalance());
		assertEquals(33, successCount.get());
		assertEquals(17, failCount.get());

	}
}

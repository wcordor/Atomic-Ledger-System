package ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import jakarta.persistence.EntityNotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest
class LedgerApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(LedgerApplicationTests.class);

	@Autowired
	private AccountRepo ar;

	@Autowired
	private UserRepo ur;

	@Autowired
	private TransferService ts;

	@Autowired
	private TransactionRepo tr;

	Account acc;
	Account acc2;
	Account a;
	Account a2;
	User user;
	User user2;

	@BeforeEach
	void setUp() {

		ar.deleteAll();
		ur.deleteAll();
		tr.deleteAll();

		acc = new Account("Checking", new BigDecimal("1000.00"), "USD");
		acc2 = new Account("Savings", new BigDecimal("0.00"), "USD");

		user = new User("Account", "Owner");
		acc.setUser(user);
		user.addAccount(acc);
		ur.save(user);
		ar.save(acc);

		user2 = new User("Account", "Owner II");
		acc2.setUser(user2);
		user2.addAccount(acc2);
		ur.save(user2);
		ar.save(acc2);

		a = ar.findById(acc.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
		a2 = ar.findById(acc2.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));

	}

	@Test
	void testAccountFunctions() {

		assertNotNull(acc.getId());
		assertEquals("Checking", acc.getName());
		assertEquals(new BigDecimal("1000.00"), acc.getBalance());
		assertEquals("USD", acc.getCurrency());
		assertTrue(user.equals(acc.getUser()));

		acc.setId(999999999999L);
		assertEquals(999999999999L, acc.getId());
		acc.setId(-7L);
		assertEquals(-7L, acc.getId());
		acc.setName("Savings");
		assertEquals("Savings", acc.getName());
		acc.setBalance(new BigDecimal("10000000000.00"));
		assertEquals(new BigDecimal("10000000000.00"), acc.getBalance());
		acc.setBalance(new BigDecimal("-3000.00"));
		assertEquals(new BigDecimal("-3000.00"), acc.getBalance());
		acc.setCurrency("GBP");
		assertEquals("GBP", acc.getCurrency());
		acc.setUser(user2);
		assertFalse(user.equals(acc.getUser()));
		assertTrue(user2.equals(acc.getUser()));

	}

	@Test
	void testAccountRepoFunctions() {

		assertEquals(1, ar.findByName("Checking").size());
		assertTrue(ar.findByName("Checking").contains(acc));
		
		assertEquals(acc, ar.findById(acc.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found")));
		assertEquals(1, ar.findByUserLastName("Owner").size());
		assertTrue(ar.findByUserLastName("Owner").contains(acc));

		List<Account> usd = ar.findByCurrency("USD");
		assertEquals(2, usd.size());
		assertTrue(usd.contains(acc) && usd.contains(acc2));
		
	}

	@Test
	void testUserFunctions() {

		assertNotNull(user.getId());
		assertEquals("Account", user.getFirstName());
		assertEquals("Owner", user.getLastName());
		assertNotNull(user2.getId());
		assertEquals("Account", user2.getFirstName());
		assertEquals("Owner II", user2.getLastName());

		assertEquals(1, user.getAccounts().size());
		assertTrue(user.getAccounts().contains(acc));
		
		user.setId(-7L);
		assertEquals(-7L, user.getId());
		user.setId(999999999999L);
		assertEquals(999999999999L, user.getId());
		user.setFirstName("User");
		user.setLastName("1");
		String name = user.getFirstName() + " " + user.getLastName();
		assertEquals("User 1", name);
		user.addAccount(acc2);

		List<Account> userAccs = user.getAccounts();
		assertEquals(2, userAccs.size());
		assertTrue(userAccs.contains(acc) && userAccs.contains(acc2));
		
	}

	@Test
	void testUserRepoFunctions() {

		assertEquals(1, ur.findByLastName("Owner").size());
		assertEquals(user, ur.findById(user.getId()).orElseThrow(() -> new EntityNotFoundException("User not found")));
		assertEquals(1, ur.findByLastName("Owner II").size());
		assertEquals(user2, ur.findById(user2.getId()).orElseThrow(() -> new EntityNotFoundException("User not found")));
	}

	@Test
	void testMoneyTransfers() {
		// acc balance: $1,000, acc2 balance: $0
		
		try {
			ts.transferMoney(a2.getId(), a.getId(), new BigDecimal("400.00"), "USD"/*, transaction*/);
		} catch (InsufficientFundsException e) {
			logger.error("ERROR: " + e.getMessage());
		}

		a = ar.findById(acc.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
		a2 = ar.findById(acc2.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));

		assertEquals(new BigDecimal("400.00"), a2.getBalance());
		assertEquals(new BigDecimal("600.00"), a.getBalance());
		assertThrows(InsufficientFundsException.class, () -> {
			ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("4000.00"), "USD"/*, transaction2*/);
		});

		try {
			ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("800.00"), "USD"/*, transaction3*/);
		} catch (InsufficientFundsException e) {
			logger.error("ERROR: " + e.getMessage());
		}

        a = ar.findWithTransactions(acc.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
        a2 = ar.findWithTransactions(acc2.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));

		assertEquals(new BigDecimal("600.00"), a.getBalance());

        List<Transaction> a_transactions = a.getTransactions();
        List<Transaction> a2_transactions = a2.getTransactions();

		Transaction transaction = a_transactions.get(0);
		assertNotNull(transaction.getId());
        assertEquals(1, a_transactions.size());
        assertEquals(1, a2_transactions.size());
		assertTrue(a_transactions.contains(transaction) && a2_transactions.contains(transaction));
		assertEquals(a.getId(), transaction.getSenderId());
		assertEquals(a2.getId(), transaction.getReceiverId());
		transaction.setId(99999L);
		assertFalse(99999L == transaction.getId());
		transaction.setReceiver(a);
		transaction.setSender(a2);
		assertEquals(a2.getId(), transaction.getReceiverId());
		assertEquals(a.getId(), transaction.getSenderId());
		assertEquals(new BigDecimal("400.00"), transaction.getAmount());
		transaction.setAmount(new BigDecimal("9000000.00"));
		assertEquals(new BigDecimal("400.00"), transaction.getAmount());
		assertEquals("USD", transaction.getCurrency());
		transaction.setCurrency("GBP");
		assertEquals("USD", transaction.getCurrency());
		assertEquals(Status.SUCCESSFUL, transaction.getStatus());
		transaction.setStatus(Status.FAILED);
		assertEquals(Status.SUCCESSFUL, transaction.getStatus());

		List<Account> transactionAccs = transaction.getAccounts();
		assertEquals(2, transactionAccs.size());
	}

	@Test
	void testConcurrencySufficient() {

		CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
			try {
				ts.transferMoney(a2.getId(), a.getId(), new BigDecimal("100.00"), "USD")/*, transaction*/;
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
			}
		});
		CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
			try {
				ts.transferMoney(a2.getId(), a.getId(), new BigDecimal("200.00"), "USD"/*, transaction2*/);
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
			}
		});
		CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
			try {
				ts.transferMoney(a2.getId(), a.getId(), new BigDecimal("500.00"), "USD"/*, 	transaction3*/);
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
			}
		});

		

		CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(future1, future2, future3);

		combinedFuture.join();

		ar.flush();

		a = ar.findById(acc.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
		a2 = ar.findById(acc2.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));

		assertTrue(future1.isDone());
		assertTrue(future2.isDone());
		assertTrue(future3.isDone());

		assertEquals(new BigDecimal("200.00"), a.getBalance());
		assertEquals(new BigDecimal("800.00"), a2.getBalance());

	}

	@Test
	void testConcurrencyInsufficient() {

		AtomicBoolean failedThread = new AtomicBoolean(false);

		CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
			try {
				ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("500.00"), "USD"/*, transaction*/);
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
				failedThread.set(true);
			}
		});
		CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
			try {
				ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("400.00"), "USD"/*, transaction2*/);
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
				failedThread.set(true);
			}
		});
		CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
			try {
				ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("300.00"), "USD"/*, transaction3*/);
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
				failedThread.set(true);
			}
		});

		CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(future1, future2, future3);
		combinedFuture.join();

		ar.flush();

		assertTrue(future1.isDone());
		assertTrue(future2.isDone());
		assertTrue(future3.isDone());

		assertEquals(true, failedThread.get());

		Account a = ar.findById(acc.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
		Account a2 = ar.findById(acc2.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));

		assertTrue(new BigDecimal("100.00").compareTo(a.getBalance()) == 0 
		|| new BigDecimal("200.00").compareTo(a.getBalance()) == 0);

		assertTrue(new BigDecimal("900.00").compareTo(a2.getBalance()) == 0 
		|| new BigDecimal("800.00").compareTo(a2.getBalance()) == 0);
	}

	@Test
	void test() {

		List<CompletableFuture<Void>> futures = new ArrayList<>();

			for (int i = 0; i < 20; i++) {
				CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
					try {
						ts.transferMoney(a2.getId(), a.getId(), new BigDecimal("10.00"), "USD");
					} catch (InsufficientFundsException e) {
						logger.warn("ERROR: " + e.getMessage());
					} catch (ObjectOptimisticLockingFailureException e) {
						logger.warn("WARNING: " + e.getMessage());
					}
				});
				futures.add(future1);

			}

			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
			ar.flush();

			a = ar.findById(acc.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
			a2 = ar.findById(acc2.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));

			assertEquals(new BigDecimal("800.00"), a.getBalance());
			assertEquals(new BigDecimal("200.00"), a2.getBalance());

	}

}

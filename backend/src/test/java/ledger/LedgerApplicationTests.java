package ledger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

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
	User user;
	User user2;
	Transaction transaction;
	Transaction transaction2;
	Transaction transaction3;

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

		transaction = new Transaction((Account) null, (Account) null, (BigDecimal) null, (String) null, (Status) null);
		transaction2 = new Transaction((Account) null, (Account) null, (BigDecimal) null, (String) null, (Status) null);
		transaction3 = new Transaction((Account) null, (Account) null, (BigDecimal) null, (String) null, (Status) null);
		tr.save(transaction);
		tr.save(transaction2);
		tr.save(transaction3);

		//Account a2FromRepo = ar.findById(acc2.getId()).get();
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
		
		assertEquals(acc, ar.findById(acc.getId()).get());
		assertEquals(1, ar.findByUserLastName("Owner").size());
		assertTrue(ar.findByUserLastName("Owner").contains(acc));

		assertEquals(2, ar.findByCurrency("USD").size());
		assertTrue(ar.findByCurrency("USD").contains(acc) 
			&& ar.findByCurrency("USD").contains(acc2));
		
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
		assertEquals(2, user.getAccounts().size());
		assertTrue(user.getAccounts().contains(acc) && user.getAccounts().contains(acc2));
		
	}

	@Test
	void testUserRepoFunctions() {

		assertEquals(1, ur.findByLastName("Owner").size());
		assertEquals(user, ur.findById(user.getId()).get());
		assertEquals(1, ur.findByLastName("Owner II").size());
		assertEquals(user2, ur.findById(user2.getId()).get());
	}

	@Test
	void testMoneyTransfers() {
		// acc balance: $1,000, acc2 balance: $0
		try {
			ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("400.00"), "USD", transaction);
		} catch (InsufficientFundsException e) {
			logger.error("ERROR: " + e.getMessage());
		}
		assertEquals(new BigDecimal("400.00"), ar.findById(acc2.getId()).get().getBalance());
		assertEquals(new BigDecimal("600.00"), ar.findById(acc.getId()).get().getBalance());
		assertThrows(InsufficientFundsException.class, () -> {
			ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("4000.00"), "USD", transaction2);
		});

		try {
			ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("800.00"), "USD", transaction3);
		} catch (InsufficientFundsException e) {
			logger.error("ERROR: " + e.getMessage());
		}

		//tr.save(transaction);

		assertEquals(new BigDecimal("600.00"), ar.findById(acc.getId()).get().getBalance());
		assertEquals(1, ar.findById(acc.getId()).get().getTransactions().size());
		assertEquals(1, ar.findById(acc2.getId()).get().getTransactions().size());
		assertTrue(ar.findById(acc.getId()).get().getTransactions().contains(transaction));
		assertTrue(ar.findById(acc2.getId()).get().getTransactions().contains(transaction));
		//assertEquals(new BigDecimal("600.00"), ar.getBalanceById(acc.getId()));

	}

	@Test
	void testTransactionFunctions() {
		assertNotNull(transaction.getId());
		assertEquals(null, transaction.getSender());
		assertEquals(null, transaction.getReceiver());
		assertEquals(null, transaction.getAmount());
		assertEquals(null, transaction.getCurrency());
		assertEquals(null, transaction.getStatus());

		try {
			ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("600.00"), "USD", transaction);
		} catch (InsufficientFundsException e) {
			logger.error("ERROR: " + e.getMessage());
		}

		assertEquals(ar.findById(acc2.getId()).get(), transaction.getReceiver());
		assertEquals(ar.findById(acc.getId()).get(), transaction.getSender());
		assertEquals(new BigDecimal("600.00"), transaction.getAmount());
		assertEquals("USD", transaction.getCurrency());
		assertEquals(Status.SUCCESSFUL, transaction.getStatus());
		assertEquals(2, transaction.getAccounts().size());
		assertTrue(transaction.getAccounts().contains(ar.findById(acc2.getId()).get()) 
		&& transaction.getAccounts().contains(ar.findById(acc.getId()).get()));

		assertEquals(new BigDecimal("600.00"), ar.findById(acc2.getId()).get().getBalance());
		assertEquals(new BigDecimal("400.00"), ar.findById(acc.getId()).get().getBalance());

		assertNotNull(transaction2.getId());
		assertEquals(null, transaction2.getSender());
		assertEquals(null, transaction2.getReceiver());
		assertEquals(null, transaction2.getAmount());
		assertEquals(null, transaction2.getCurrency());
		assertEquals(null, transaction2.getStatus());

		try {
			ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("500.00"), "USD", transaction2);
		} catch (InsufficientFundsException e) {
			logger.error("ERROR: " + e.getMessage());
		}

		assertEquals(ar.findById(acc2.getId()).get(), transaction2.getReceiver());
		assertEquals(ar.findById(acc.getId()).get(), transaction2.getSender());
		assertEquals(new BigDecimal("500.00"), transaction2.getAmount());
		assertEquals("USD", transaction2.getCurrency());
		assertEquals(Status.FAILED, transaction2.getStatus());
		assertEquals(2, transaction2.getAccounts().size());
		assertTrue(transaction2.getAccounts().contains(ar.findById(acc2.getId()).get()) 
		&& transaction2.getAccounts().contains(ar.findById(acc.getId()).get()));
	}

	@Test
	void testConcurrencySufficient() {

		CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
			try {
				ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("100.00"), "USD", transaction);
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
			}
		});
		CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
			try {
				ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("200.00"), "USD", transaction2);
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
			}
		});
		CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
			try {
				ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("500.00"), "USD", 	transaction3);
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
			}
		});

		CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(future1, future2, future3);

		combinedFuture.join();

		ar.flush();

		assertTrue(future1.isDone());
		assertTrue(future2.isDone());
		assertTrue(future3.isDone());

		assertEquals(new BigDecimal("200.00"), ar.findById(acc.getId()).get().getBalance());
		assertEquals(new BigDecimal("800.00"), ar.findById(acc2.getId()).get().getBalance());

	}

	@Test
	void testConcurrencyInsufficient() {

		AtomicBoolean failedThread = new AtomicBoolean(false);

		CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
			try {
				ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("500.00"), "USD", transaction);
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
				failedThread.set(true);
			}
		});
		CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
			try {
				ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("400.00"), "USD", transaction2);
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
				failedThread.set(true);
			}
		});
		CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
			try {
				ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("300.00"), "USD", transaction3);
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

		assertTrue(new BigDecimal("100.00").compareTo(ar.findById(acc.getId()).get().getBalance()) == 0 
		|| new BigDecimal("200.00").compareTo(ar.findById(acc.getId()).get().getBalance()) == 0);

		assertTrue(new BigDecimal("900.00").compareTo(ar.findById(acc2.getId()).get().getBalance()) == 0 
		|| new BigDecimal("800.00").compareTo(ar.findById(acc2.getId()).get().getBalance()) == 0);
	}

}

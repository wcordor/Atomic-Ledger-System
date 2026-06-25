package clean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootTest
class CleanApplicationTests {

	private static final Logger logger = LoggerFactory.getLogger(CleanApplicationTests.class);

	@Autowired
	private AccountRepo ar;

	@Autowired
	private UserRepo ur;

	@Autowired
	private TransferService ts;

	Account acc;
	Account acc2;
	User user;
	User user2;

	@BeforeEach
	void setUp() {

		ar.deleteAll();
		ur.deleteAll();

		acc = new Account("Checking", new BigDecimal("1000.00"), "USD");
		acc2 = new Account("Savings", new BigDecimal("0.00"), "USD");
		ar.save(acc2);

		user = new User("Account", "Owner");
		acc.setUser(user);
		user.addAccount(acc);
		ur.save(user);
		ar.save(acc);

		user2 = new User("Account", "Owner II");
		ur.save(user2);
	}

	@Test
	void testAccountFunctions() {

		assertNotNull(acc.getId());
		assertEquals("Checking", acc.getName());
		assertEquals(new BigDecimal("1000.00"), acc.getBalance());
		assertEquals("USD", acc.getCurrency());
		assertTrue(user == acc.getUser());
		assertEquals("Account[name=Checking, id=" + acc.getId() + ", user=Account Owner, balance=1,000.00 USD]", acc.toString());

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
		assertFalse(user == acc.getUser());
		assertTrue(user2 == acc.getUser());
		assertEquals("Account[name=Savings, id=-7, user=Account Owner II, balance=-3,000.00 GBP]", acc.toString());

	}

	@Test
	void testAccountRepoFunctions() {

		List<Account> accList = new ArrayList<>();
		accList.add(acc);
		assertEquals(accList.toString(), ar.findByName("Checking").toString());
		
		assertEquals(acc.toString(), ar.findById(acc.getId()).get().toString());
		assertEquals(accList.toString(), ar.findByUserLastName("Owner").toString());

		accList.add(acc2);
		accList.sort(Comparator.comparing(Account::getId));
		ar.findByCurrency("USD").sort(Comparator.comparing(Account::getId));
		assertEquals(accList.toString(), ar.findByCurrency("USD").toString());
		
	}

	@Test
	void testUserFunctions() {

		assertNotNull(user.getId());
		assertEquals("Account", user.getFirstName());
		assertEquals("Owner", user.getLastName());

		List<Account> userAccs = new ArrayList<>();
		userAccs.add(acc);
		assertEquals(userAccs.toString(), user.getAccounts().toString());

		assertEquals("user[name=Account Owner, id=" + user.getId() + ", # of accounts=1]", user.toString());
		
		user.setId(-7L);
		assertEquals(-7L, user.getId());
		user.setId(999999999999L);
		assertEquals(999999999999L, user.getId());
		user.setFirstName("User");
		user.setLastName("1");
		String name = user.getFirstName() + " " + user.getLastName();
		assertEquals("User 1", name);
		user.addAccount(acc2);
		userAccs.add(acc2);
		assertEquals(userAccs.toString(), user.getAccounts().toString());
		assertEquals("user[name=User 1, id=999999999999, # of accounts=2]", user.toString());
		
	}

	@Test
	void testUserRepoFunctions() {

		List<User> userList = new ArrayList<>();
		userList.add(user);
		assertEquals(userList.toString(), ur.findByLastName("Owner").toString());
		assertEquals(user.toString(), ur.findById(user.getId()).get().toString());
	}

	@Test
	void testMoneyTransfers() {
		//acc balance: $1,000, acc2 balance: $0
		try {
			ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("400.00"));
		} catch (InsufficientFundsException e) {
			logger.error("ERROR: " + e.getMessage());
		}
		assertEquals(new BigDecimal("400.00"), ar.findById(acc2.getId()).get().getBalance());
		assertEquals(new BigDecimal("600.00"), ar.findById(acc.getId()).get().getBalance());
		assertThrows(InsufficientFundsException.class, () -> {
			ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("4000.00"));
		});

		try {
			ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("800.00"));
		} catch (InsufficientFundsException e) {
			logger.error("ERROR: " + e.getMessage());
		}

		assertEquals(new BigDecimal("600.00"), ar.findById(acc.getId()).get().getBalance());

	}

	@Test
	void testConcurrencySufficient() {

		CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
			try {
				ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("100.00"));
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
			}
		});
		CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
			try {
				ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("200.00"));
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
			}
		});
		CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
			try {
				ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("500.00"));
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
			}
		});

		CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(future1, future2, future3);

		combinedFuture.join();

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
				ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("500.00"));
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
				failedThread.set(true);
			}
		});
		CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
			try {
				ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("400.00"));
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
				failedThread.set(true);
			}
		});
		CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> {
			try {
				ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("300.00"));
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
				failedThread.set(true);
			}
		});

		CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(future1, future2, future3);

		combinedFuture.join();

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

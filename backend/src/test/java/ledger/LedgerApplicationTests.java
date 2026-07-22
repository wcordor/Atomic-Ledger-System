package ledger;

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
	private AccountRepo ar;

	@Autowired
	private UserRepo ur;

	@Autowired
	private TransferService ts;

	@Autowired
	private TransactionRepo tr;

	@Autowired
	private AccountService as;

	private Account acc;
	private Account acc2;
	private User user;
	private User user2;

	@BeforeEach
	void setUp() {

		ar.deleteAll();
		ur.deleteAll();
		tr.deleteAll();

		user = new User("Account", "Owner");
		ur.save(user);
		acc = as.createAccount(user.getId(), "Savings", new BigDecimal("1000.00"), "USD");
		user = ur.findById(user.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
		

		user2 = new User("Account", "Owner II");
		ur.save(user2);
		acc2 = as.createAccount(user2.getId(), "Checking", new BigDecimal("200.00"), "USD");

	}

	@Test
	void testAccountFunctions() {

		assertNotNull(acc.getId());
		assertEquals("Savings", acc.getName());
		assertEquals(new BigDecimal("1000.00"), acc.getBalance());
		assertEquals("USD", acc.getCurrency());
		assertTrue(user.getId().equals(acc.getUser().getId()));

		acc.setId(999999999999L);
		assertNotEquals(999999999999L, acc.getId());
		acc.setName("Checking");
		assertEquals("Checking", acc.getName());

		// FIXME: replace setBalance() with debit() and credit()
		acc.setBalance(new BigDecimal("10000000000.00"));
		assertEquals(new BigDecimal("10000000000.00"), acc.getBalance());
		acc = ar.findById(acc.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
		assertEquals(new BigDecimal("1500.00"), acc.getBalance());
		acc.setBalance(new BigDecimal("-3000.00"));
		assertEquals(new BigDecimal("1500.00"), acc.getBalance());


		acc.setCurrency("GBP");
		assertEquals("GBP", acc.getCurrency());
		acc.setUser(user2);
		assertFalse(user2.equals(acc.getUser()));
		assertTrue(user.equals(acc.getUser()));

	}

	@Test
	void testAccountRepoFunctions() {

		assertEquals(1, ar.findByName("Savings").size());
		assertTrue(ar.findByName("Savings").contains(acc));
		
		assertEquals(acc,
			ar.findById(acc.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found")));
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
			ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("400.00"), "USD"/*, transaction*/);
		} catch (InsufficientFundsException e) {
			logger.error("ERROR: " + e.getMessage());
		}

		acc = ar.findById(acc.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
		acc2 = ar.findById(acc2.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));

		assertEquals(new BigDecimal("400.00"), acc2.getBalance());
		assertEquals(new BigDecimal("600.00"), acc.getBalance());
		
		assertThrows(InsufficientFundsException.class, () -> {
			ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("4000.00"), "USD");
		});

		try {
			ts.transferMoney(acc2.getId(), acc.getId(), new BigDecimal("800.00"), "USD");
		} catch (InsufficientFundsException e) {
			logger.error("ERROR: " + e.getMessage());
		}

        acc = ar.findWithTransactions(acc.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
        acc2 = ar.findWithTransactions(acc2.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));

		assertEquals(new BigDecimal("600.00"), acc.getBalance());

        List<Transaction> a_transactions = acc.getTransactions();
        List<Transaction> a2_transactions = acc2.getTransactions();

		Transaction transaction = a_transactions.get(0);
		assertNotNull(transaction.getId());
        assertEquals(1, a_transactions.size());
        assertEquals(1, a2_transactions.size());
		assertTrue(a_transactions.contains(transaction) && a2_transactions.contains(transaction));
		assertEquals(acc.getId(), transaction.getSenderId());
		assertEquals(acc2.getId(), transaction.getReceiverId());
		transaction.setId(99999L);
		assertFalse(99999L == transaction.getId());
		transaction.setReceiver(acc);
		transaction.setSender(acc2);
		assertEquals(acc2.getId(), transaction.getReceiverId());
		assertEquals(acc.getId(), transaction.getSenderId());
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
		assertTrue(transactionAccIds.contains(acc.getId()) && transactionAccIds.contains(acc2.getId()));
	}

	@Test
	void testConcurrencySufficient() {

		List<CompletableFuture<Void>> futures = new ArrayList<>();

		// generate 50 threads
		for (int i = 0; i < 50; i++) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				try {
					ts.transferMoney(a2.getId(), a.getId(), new BigDecimal("10.00"), "USD");
				} catch (InsufficientFundsException e) {
					logger.error("ERROR: " + e.getMessage());
				}
			});
			futures.add(future);
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		ar.flush();

		a = ar.findById(acc.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
		a2 = ar.findById(acc2.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));

		assertEquals(new BigDecimal("500.00"), a.getBalance());
		assertEquals(new BigDecimal("500.00"), a2.getBalance());

	}
	
	@Test
	void testConcurrencyInsufficient() {

		List<CompletableFuture<Void>> futures = new ArrayList<>();

		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failCount = new AtomicInteger(0);

		for (int i = 0; i < 50; i++) {
			CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
				try {
					ts.transferMoney(a2.getId(), a.getId(), new BigDecimal("30.00"), "USD");
					successCount.incrementAndGet();
				} catch (InsufficientFundsException e) {
					logger.error("ERROR: " + e.getMessage());
					failCount.incrementAndGet();
				}
			});
			futures.add(future);
		}

		CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
		ar.flush();

		a = ar.findById(acc.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
		a2 = ar.findById(acc2.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));

		assertEquals(new BigDecimal("10.00"), a.getBalance());
		assertEquals(new BigDecimal("990.00"), a2.getBalance());
		assertEquals(33, successCount.get());
		assertEquals(17, failCount.get());

	}

	@Test
	void au() {

		as.createAccount(user.getId(), "Checking", new BigDecimal("500.00"), "USD");
		user = ur.findById(user.getId()).orElseThrow(() -> new EntityNotFoundException("User not found"));
		Account acc5 = user.getAccounts().get(0);

		assertEquals(user, acc5.getUser());
		assertEquals("Checking", acc5.getName());
		assertEquals(new BigDecimal("500.00"), acc5.getBalance());
		assertEquals("USD", acc5.getCurrency());

		Account ac = new Account(user, "Savings", new BigDecimal("1000.00"), "USD");
		ar.save(ac);
		user = ur.findById(user.getId()).orElseThrow(() -> new EntityNotFoundException("User not found"));

		assertEquals(user, ac.getUser());
		assertEquals("Savings", ac.getName());
		assertEquals(new BigDecimal("1000.00"), ac.getBalance());
		assertEquals("USD", acc5.getCurrency());

		List<Account> accList = user.getAccounts();
		assertEquals(2, accList.size());
		
		assertTrue(accList.contains(acc5) && accList.contains(ac));
	}
}

package com.github.wcordor.ledger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.retry.annotation.EnableRetry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableRetry
public class LedgerApplication {

	private static final Logger logger = LoggerFactory.getLogger(LedgerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(LedgerApplication.class, args);
	}

	@Bean
	public CommandLineRunner demo(UserRepository userRepository, AccountRepository accountRepository,
		TransactionService transactionService, AccountService accountService, UserService userService) {

		return (args) -> {

			User user1 = userService.createUser(UUID.randomUUID().toString(), "John", "Smith");
			Long user1_id = user1.getId();

			Account account1 = accountService.createAccount(UUID.randomUUID().toString(), user1_id,
				"Savings", new BigDecimal("5000.00"), "GBP");

			Account account2 = accountService.createAccount(UUID.randomUUID().toString(), user1_id,
				"Checking", new BigDecimal("1000.00"), "GBP");

			User user2 = userService.createUser(UUID.randomUUID().toString(), "Bernard", "Jones");
			Long user2_id = user2.getId();

			Account account3 = accountService.createAccount(UUID.randomUUID().toString(), user2_id,
				"Investment", new BigDecimal("15000.00"), "USD");

			Account account4 = accountService.createAccount(UUID.randomUUID().toString(), user2_id, 
				"Savings", new BigDecimal("7000.00"), "USD");

			Account account5 = accountService.createAccount(UUID.randomUUID().toString(), user2_id,
				"Checking", new BigDecimal("3000.00"), "USD");

			User user3 = userService.createUser(UUID.randomUUID().toString(), "Deborah", "Adams");
			Long user3_id = user3.getId();

			Account account6 = accountService.createAccount(UUID.randomUUID().toString(), user3_id,
				"Savings", new BigDecimal("3000.00"), "USD");

			Account account7 = accountService.createAccount(UUID.randomUUID().toString(), user3_id,
				"Checking", new BigDecimal("1000.00"), "USD");

			User user4 = userService.createUser(UUID.randomUUID().toString(), "Mary", "Johnson");
			Long user4_id = user4.getId();

			Account account8 = accountService.createAccount(UUID.randomUUID().toString(), user4_id,
				"Savings", new BigDecimal("5500.00"), "USD");

			Account account9 = accountService.createAccount(UUID.randomUUID().toString(), user4_id,
				"Checking", new BigDecimal("1500.00"), "USD");

			logger.info("");
			logger.info("List of Preloaded Users:");
			logger.info("------------------------");
			userRepository.findAll().forEach(user -> {
				logger.info(user.toString());
			});
			logger.info("");

			logger.info("List of Preloaded Accounts:");
			logger.info("---------------------------");
			accountRepository.findAll().forEach(acc -> {
				logger.info(acc.toString());
			});
			logger.info("");

			logger.info("Accounts that use USD:");
			logger.info("----------------------");
			accountRepository.findByCurrency("USD").forEach(usd -> {
				logger.info(usd.toString());
			});
			logger.info("");

			logger.info("Accounts that use GBP:");
			logger.info("----------------------");
			accountRepository.findByCurrency("GBP").forEach(gbp -> {
				logger.info(gbp.toString());
			});
			logger.info("");

			Long account5_id = account5.getId();
			Long account7_id = account7.getId();

			account5 = accountService.getAccount(account5_id, user2_id);
			account7 = accountService.getAccount(account7_id, user3_id);
			BigDecimal account5_bal = account5.getBalance();
			BigDecimal account7_bal = account7.getBalance();
			String account5_currency = account5.getCurrency();
			String account7_currency = account7.getCurrency();

			try {
				logger.info("Account " + account5_id + " (B. Jones) transfer 500 USD to Account " + account7_id + " (D. Adams)"); 
				logger.info("-------------------------------------------------------------");
				logger.info(String.format("Balances before transfer: Account %d - %,.2f %s, Account %d - %,.2f %s",
					account5_id, account5_bal, account5_currency, account7_id, account7_bal, account7_currency));
				transactionService.moneyTransfer("idempotency10", user2_id, account5_id, account7_id, new BigDecimal("500.00"), "USD");
			} catch (InsufficientFundsException e) {
				logger.info("");
				logger.error("ERROR: " + e.getMessage());
				logger.info("");
			}

			account5 = accountService.getAccount(account5_id, user2_id);
			account7 = accountService.getAccount(account7_id, user3_id);
			account5_bal = account5.getBalance();
			account7_bal = account7.getBalance();

			logger.info(String.format("Balances after transfer: Account %d - %,.2f %s, Account %d - %,.2f %s",
				account5_id, account5_bal, account5_currency, account7_id, account7_bal, account7_currency));
			if (account5_bal.compareTo(new BigDecimal("2500.00")) == 0
				&& account7_bal.compareTo(new BigDecimal("1500.00")) == 0) {
				logger.info("***********************");
				logger.info("Transaction successful.");
				logger.info("***********************");
				logger.info("");
			}

			Transaction transaction = account5.getTransactions().get(0);
			logger.info("Transaction Info:");
			logger.info(transaction.toString());

			try {
				logger.info("Account " + account7_id + " (D. Adams) transfer 2,000 USD to Account " + account5_id + " (B. Jones)"); 
				logger.info("---------------------------------------------------------------");	
				logger.info(String.format("Balances before transfer: Account %d - %,.2f %s, Account %d - %,.2f %s",
					account7_id, account7_bal, account7_currency, account5_id, account5_bal, account5_currency));
				transactionService.moneyTransfer("idempotency11", user3_id, account7_id, account5_id, new BigDecimal("2000.00"), "USD");				
				logger.info("");
			} catch (InsufficientFundsException e) {
				logger.info("");
				logger.error("ERROR: " + e.getMessage());
				logger.info("");
			}

			account5 = accountService.getAccount(account5_id, user2_id);
			account7 = accountService.getAccount(account7_id, user3_id);
			BigDecimal account5_bal_rolledBack = account5.getBalance();
			BigDecimal account7_bal_rolledBack = account7.getBalance();

			logger.info(String.format("Balances after transfer: Account %d - %,.2f %s, Account %d - %,.2f %s",
				account7_id, account7_bal, account7_currency, account5_id, account5_bal, account5_currency));

			if (account5_bal_rolledBack.compareTo(account5_bal) == 0
			&& account7_bal_rolledBack.compareTo(account7_bal) == 0) {
				logger.info("********************");
				logger.info("Rollback successful.");
				logger.info("********************");
				logger.info("");
			}

			Long account9_id = account9.getId();
			account9 = accountService.getAccount(account9_id, user4_id);
			BigDecimal account9_bal = account9.getBalance();
			String account9_currency = account9.getCurrency();
			
			logger.info("40 simultaneous transactions from Account " + account5_id + " (B. Jones) and Account " + account7_id);
			logger.info("(D. Adams), to Account " + account9_id + " (M. Johnson)");
			logger.info("--------------------------------------------------------------------");
			logger.info("Balances before transfers:");
			logger.info("");
			logger.info(String.format("Account %d - %,.2f %s", account9_id, account9_bal, account9_currency));
			logger.info(String.format("Account %d - %,.2f %s", account5_id, account5_bal, account5_currency));
			logger.info(String.format("Account %d - %,.2f %s", account7_id, account7_bal, account7_currency));
			logger.info("");

			List<CompletableFuture<Void>> futures = new ArrayList<>();

			for (int i = 0; i < 20; i++) {
				CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
					try {
						transactionService.moneyTransfer("idempotency12", user2_id, account5_id, account9_id, new BigDecimal("50.00"), "USD");
					} catch (InsufficientFundsException e) {
						logger.info("");
						logger.error("ERROR: " + e.getMessage());
						logger.info("");
					}
				});
				futures.add(future1);

				CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
					try {
						transactionService.moneyTransfer("idempotency13", user3_id, account7_id, account9_id, new BigDecimal("25.00"), "USD");
					} catch (InsufficientFundsException e) {
						logger.info("");
						logger.error("ERROR: " + e.getMessage());
						logger.info("");
					}
				});
				futures.add(future2);

			}

			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
			accountRepository.flush();

			account5 = accountService.getAccount(account5_id, user2_id);
			account7 = accountService.getAccount(account7_id, user3_id);
			account9 = accountService.getAccount(account9_id, user4_id);

			account9_bal = account9.getBalance();
			account5_bal = account5.getBalance();
			account7_bal = account7.getBalance();

			logger.info("Balances after transfers:");
			logger.info("");
			logger.info(String.format("Account %d - %,.2f %s", account9_id, account9_bal, account9_currency));
			logger.info(String.format("Account %d - %,.2f %s", account5_id, account5_bal, account5_currency));
			logger.info(String.format("Account %d - %,.2f %s", account7_id, account7_bal, account7_currency));
			if (account9_bal.compareTo(new BigDecimal("3000.00")) == 0
			&& account5_bal.compareTo(new BigDecimal("1500.00")) == 0
			&& account7_bal.compareTo(new BigDecimal("1000.00")) == 0) {
				logger.info("***********************************");
				logger.info("Concurrent transactions successful.");
				logger.info("***********************************");
				logger.info("Account " + account9_id + " total transactions: " + account9.getTransactions().size());
				logger.info("Account " + account5_id + " total transactions: " + account5.getTransactions().size());
				logger.info("Account " + account7_id + " total transactions: " + account7.getTransactions().size());
				
			}
		};
	}

}

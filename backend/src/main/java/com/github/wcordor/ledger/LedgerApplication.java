package com.github.wcordor.ledger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.retry.annotation.EnableRetry;

import com.github.wcordor.ledger.ledger.Account;
import com.github.wcordor.ledger.ledger.AccountService;
import com.github.wcordor.ledger.ledger.TransferService;

import jakarta.persistence.EntityNotFoundException;

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
	public CommandLineRunner demo(UserRepo uRepo, AccountRepo aRepo, TransferService service, AccountService accService) {
		return (args) -> {

			User user1 = new User("John", "Smith");
			uRepo.save(user1);
			Long user1_id = user1.getId();

			Account account1 = accService.createAccount(user1_id, "Savings", new BigDecimal("5000.00"), "GBP");
			Account account2 = accService.createAccount(user1_id, "Checking", new BigDecimal("1000.00"), "GBP");

			User user2 = new User("Bernard", "Jones");
			uRepo.save(user2);
			Long user2_id = user2.getId();

			Account account3 = accService.createAccount(user2_id, "Investment", new BigDecimal("15000.00"), "USD");
			Account account4 = accService.createAccount(user2_id, "Savings", new BigDecimal("7000.00"), "USD");
			Account account5 = accService.createAccount(user2_id, "Checking", new BigDecimal("3000.00"), "USD");

			User user3 = new User("Deborah", "Adams");
			uRepo.save(user3);
			Long user3_id = user3.getId();

			Account account6 = accService.createAccount(user3_id, "Savings", new BigDecimal("3000.00"), "USD");
			Account account7 = accService.createAccount(user3_id, "Checking", new BigDecimal("1000.00"), "USD");

			User user4 = new User("Mary", "Johnson");
			uRepo.save(user4);
			Long user4_id = user4.getId();

			Account account8 = accService.createAccount(user4_id, "Savings", new BigDecimal("5500.00"), "USD");
			Account account9 = accService.createAccount(user4_id, "Checking", new BigDecimal("1500.00"), "USD");

			logger.info("");
			logger.info("List of Preloaded Users:");
			logger.info("------------------------");
			uRepo.findAll().forEach(user -> {
				logger.info(user.toString());
			});
			logger.info("");

			logger.info("List of Preloaded Accounts:");
			logger.info("---------------------------");
			aRepo.findAll().forEach(acc -> {
				logger.info(acc.toString());
			});
			logger.info("");

			logger.info("Accounts that use USD:");
			logger.info("----------------------");
			aRepo.findByCurrency("USD").forEach(usd -> {
				logger.info(usd.toString());
			});
			logger.info("");

			logger.info("Accounts that use GBP:");
			logger.info("----------------------");
			aRepo.findByCurrency("GBP").forEach(gbp -> {
				logger.info(gbp.toString());
			});
			logger.info("");

			Account bj_checking = aRepo.findWithTransactions(acc5.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
			Account da_checking = aRepo.findWithTransactions(acc7.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));

			BigDecimal bj_checkingBal = bj_checking.getBalance();
			BigDecimal da_checkingBal = da_checking.getBalance();
			String bj_checkingCurrency = bj_checking.getCurrency();
			String da_checkingCurrency = da_checking.getCurrency();
			try {
				logger.info("Account " + bj_checking.getId() + " (B. Jones) transfer 1,000.00 USD to Account " + da_checking.getId() + " (D. Adams)"); 
				logger.info("------------------------------------------------------------------");
				logger.info(String.format("Balances before transfer: Account %d - %,.2f %s, Account %d - %,.2f %s",
				bj_checking.getId(), bj_checkingBal, bj_checkingCurrency, da_checking.getId(), da_checkingBal, da_checkingCurrency));
				service.transferMoney(da_checking.getId(), bj_checking.getId(), new BigDecimal("1000.00"), "USD");
			} catch (InsufficientFundsException e) {
				logger.info("");
				logger.error("ERROR: " + e.getMessage());
				logger.info("");
			}

			bj_checking = aRepo.findWithTransactions(acc5.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
			da_checking = aRepo.findWithTransactions(acc7.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));

			bj_checkingBal = bj_checking.getBalance();
			da_checkingBal = da_checking.getBalance();
			logger.info(String.format("Balances before transfer: Account %d - %,.2f %s, Account %d - %,.2f %s",
				bj_checking.getId(), bj_checkingBal, bj_checkingCurrency, da_checking.getId(), da_checkingBal, da_checkingCurrency));
			if (bj_checkingBal.compareTo(new BigDecimal("5500.00")) == 0
			&& da_checkingBal.compareTo(new BigDecimal("4000.00")) == 0) {
				logger.info("***********************");
				logger.info("Transaction successful.");
				logger.info("***********************");
				logger.info("");
			}

			Transaction transaction = bj_checking.getTransactions().get(0);
			logger.info("Transaction Info:");
			logger.info(transaction.toString());

			try {
				logger.info("Account " + da_checking.getId() + " (D. Adams) transfer 6,000.00 USD to Account " + bj_checking.getId() + " (B. Jones)"); 
				logger.info("------------------------------------------------------------------");	
				logger.info(String.format("Balances before transfer: Account %d - %,.2f %s, Account %d - %,.2f %s",
				da_checking.getId(), da_checkingBal, da_checkingCurrency, bj_checking.getId(), bj_checkingBal, bj_checkingCurrency));
				service.transferMoney(bj_checking.getId(), da_checking.getId(), new BigDecimal("6000.00"), "USD");				
				logger.info("");
			} catch (InsufficientFundsException e) {
				logger.info("");
				logger.error("ERROR: " + e.getMessage());
				logger.info("");
			}

			BigDecimal da_checkingBal_rolledBack = da_checking.getBalance();
			BigDecimal bj_checkingBal_rolledBack = bj_checking.getBalance();
			logger.info(String.format("Balances before transfer: Account %d - %,.2f %s, Account %d - %,.2f %s",
				da_checking.getId(), da_checkingBal, da_checkingCurrency, bj_checking.getId(), bj_checkingBal, bj_checkingCurrency));

			if (da_checkingBal_rolledBack.compareTo(da_checkingBal) == 0
			&& bj_checkingBal_rolledBack.compareTo(bj_checkingBal) == 0) {
				logger.info("********************");
				logger.info("Rollback successful.");
				logger.info("********************");
				logger.info("");
			}

			Account mj_checking = aRepo.findWithTransactions(acc9.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
			
			logger.info("40 simultaneous transactions from Account " + bj_checking.getId() + " (B. Jones) and Account " + da_checking.getId());
			logger.info("(D. Adams), to Account " + mj_checking.getId() + " (M. Johnson)");
			logger.info("--------------------------------------------------------------------");
			logger.info("Balances before transfers:");
			logger.info("");
			logger.info(String.format("Account %d - %,.2f %s", mj_checking.getId(), mj_checking.getBalance(), mj_checking.getCurrency()));
			logger.info(String.format("Account %d - %,.2f %s", bj_checking.getId(), bj_checking.getBalance(), bj_checking.getCurrency()));
			logger.info(String.format("Account %d - %,.2f %s", da_checking.getId(), da_checking.getBalance(), da_checking.getCurrency()));
			logger.info("");

			List<CompletableFuture<Void>> futures = new ArrayList<>();

			final Long bj_checkingId = bj_checking.getId();
			final Long mj_checkingId = mj_checking.getId();
			final Long da_checkingId = da_checking.getId();

			for (int i = 0; i < 20; i++) {
				CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> {
					try {
						service.transferMoney(mj_checkingId, bj_checkingId, new BigDecimal("200.00"), "USD");
					} catch (InsufficientFundsException e) {
						logger.info("");
						logger.error("ERROR: " + e.getMessage());
						logger.info("");
					}
				});
				futures.add(future1);

				CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> {
					try {
						service.transferMoney(mj_checkingId, da_checkingId, new BigDecimal("100.00"), "USD");
					} catch (InsufficientFundsException e) {
						logger.info("");
						logger.error("ERROR: " + e.getMessage());
						logger.info("");
					}
				});
				futures.add(future2);

			}

			logger.info("");

			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
			aRepo.flush();

			bj_checking = aRepo.findWithTransactions(acc5.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
			mj_checking = aRepo.findWithTransactions(acc9.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
			da_checking = aRepo.findWithTransactions(acc7.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));

			BigDecimal mj_checkingBal = mj_checking.getBalance();
			bj_checkingBal = bj_checking.getBalance();
			da_checkingBal = da_checking.getBalance();

			logger.info("Balances after transfers:");
			logger.info("");
			logger.info(String.format("Account %d - %,.2f %s", mj_checking.getId(), mj_checking.getBalance(), mj_checking.getCurrency()));
			logger.info(String.format("Account %d - %,.2f %s", bj_checking.getId(), bj_checking.getBalance(), bj_checking.getCurrency()));
			logger.info(String.format("Account %d - %,.2f %s", da_checking.getId(), da_checking.getBalance(), da_checking.getCurrency()));
			if (mj_checkingBal.compareTo(new BigDecimal("15340.11")) == 0
			&& bj_checkingBal.compareTo(new BigDecimal("1500.00")) == 0
			&& da_checkingBal.compareTo(new BigDecimal("2000.00")) == 0) {
				logger.info("***********************************");
				logger.info("Concurrent transactions successful.");
				logger.info("***********************************");
				logger.info("Account " + mj_checkingId + " total transactions: " + mj_checking.getTransactions().size());
				logger.info("Account " + bj_checkingId + " total transactions: " + bj_checking.getTransactions().size());
				logger.info("Account " + da_checkingId + " total transactions: " + da_checking.getTransactions().size());
				
			}
		};
	}

}

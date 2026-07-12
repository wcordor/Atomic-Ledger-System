package ledger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.retry.annotation.EnableRetry;

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
	public CommandLineRunner demo(UserRepo uRepo, AccountRepo aRepo, TransferService service, TransferRetryListener retryListener) {
		return (args) -> {

			User user1 = new User("John", "Smith");

			Account acc1 = new Account("Savings", new BigDecimal("9121.45"), "GBP");
			Account acc2 = new Account("Checking", new BigDecimal("2500.00"), "GBP");
			
			acc1.setUser(user1);
			acc2.setUser(user1);
			user1.addAccount(acc1);
			user1.addAccount(acc2);

			uRepo.save(user1);
			aRepo.save(acc1);
			aRepo.save(acc2);

			User user2 = new User("Bernard", "Jones");

			Account acc3 = new Account("Investment", new BigDecimal("90956.02"), "USD");
			Account acc4 = new Account("Savings", new BigDecimal("15643.98"), "USD");
			Account acc5 = new Account("Checking", new BigDecimal("6500.00"), "USD");

			acc3.setUser(user2);
			acc4.setUser(user2);
			acc5.setUser(user2);
			user2.addAccount(acc3);
			user2.addAccount(acc4);
			user2.addAccount(acc5);

			uRepo.save(user2);
			aRepo.save(acc3);
			aRepo.save(acc4);
			aRepo.save(acc5);

			User user3 = new User("Deborah", "Adams");

			Account acc6 = new Account("Savings", new BigDecimal("12255.68"), "USD");
			Account acc7 = new Account("Checking", new BigDecimal("3000.00"), "USD");

			acc6.setUser(user3);
			acc7.setUser(user3);
			user3.addAccount(acc6);
			user3.addAccount(acc7);

			uRepo.save(user3);
			aRepo.save(acc6);
			aRepo.save(acc7);

			User user4 = new User("Mary", "Johnson");

			Account acc8 = new Account("Savings", new BigDecimal("30383.59"), "USD");
			Account acc9 = new Account("Checking", new BigDecimal("9340.11"), "USD");

			acc8.setUser(user4);
			acc9.setUser(user4);
			user4.addAccount(acc8);
			user4.addAccount(acc9);

			uRepo.save(user4);
			aRepo.save(acc8);
			aRepo.save(acc9);

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

			Account bj_checking = aRepo.findById(acc5.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
			Account da_checking = aRepo.findById(acc7.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));

			BigDecimal bj_checkingBal = bj_checking.getBalance();
			BigDecimal da_checkingBal = da_checking.getBalance();
			String bj_checkingCurrency = bj_checking.getCurrency();
			String da_checkingCurrency = da_checking.getCurrency();
			try {
				logger.info("B. Jones transfer 1,000.00 USD to D. Adams"); 
				logger.info("------------------------------------------");	
				logger.info(String.format("Balances before transfer: B. Jones - %,.2f %s, D. Adams - %,.2f %s",
				bj_checkingBal, bj_checkingCurrency, da_checkingBal, da_checkingCurrency));
				Transaction transaction = new Transaction((Account) null, (Account) null, null, null, null);
				service.transferMoney(da_checking.getId(), bj_checking.getId(), new BigDecimal("1000.00"), "USD"/*, transaction*/);
			} catch (InsufficientFundsException e) {
				logger.info("");
				logger.error("ERROR: " + e.getMessage());
				logger.info("");
			}

			bj_checking = aRepo.findById(acc5.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
			da_checking = aRepo.findById(acc7.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));

			bj_checkingBal = bj_checking.getBalance();
			da_checkingBal = da_checking.getBalance();
			logger.info(String.format("Balances after transfer: B. Jones - %,.2f %s, D. Adams - %,.2f %s",
			bj_checkingBal, bj_checkingCurrency, da_checkingBal, da_checkingCurrency));
			if (bj_checkingBal.compareTo(new BigDecimal("5500.00")) == 0
			&& da_checkingBal.compareTo(new BigDecimal("4000.00")) == 0) {
				logger.info("************************");
				logger.info("Transaction successful.");
				logger.info("************************");
				logger.info("");
			}

			try {
				logger.info("D. Adams transfer 6,000.00 USD to B. Jones"); 
				logger.info("------------------------------------------");	
				logger.info(String.format("Balances before transfer: D. Adams - %,.2f %s, B. Jones - %,.2f %s",
				da_checkingBal, da_checkingCurrency, bj_checkingBal, bj_checkingCurrency));
				Transaction transaction2 = new Transaction((Account) null, (Account) null, null, null, null);
				service.transferMoney(bj_checking.getId(), da_checking.getId(), new BigDecimal("6000.00"), "USD"/*, transaction2*/);				
				logger.info("");
			} catch (InsufficientFundsException e) {
				logger.info("");
				logger.error("ERROR: " + e.getMessage());
				logger.info("");
			}

			BigDecimal da_checkingBal_rolledBack = da_checking.getBalance();
			BigDecimal bj_checkingBal_rolledBack = bj_checking.getBalance();
			logger.info(String.format("Balances after transfer: D. Adams - %,.2f %s, B. Jones - %,.2f %s",
				da_checkingBal_rolledBack, da_checkingCurrency, bj_checkingBal_rolledBack, bj_checkingCurrency));

			if (da_checkingBal_rolledBack.compareTo(da_checkingBal) == 0
			&& bj_checkingBal_rolledBack.compareTo(bj_checkingBal) == 0) {
				logger.info("*********************");
				logger.info("Rollback successful.");
				logger.info("*********************");
				logger.info("");
			}

			Account mj_checking = aRepo.findById(acc9.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
			
			logger.info("40 simultaneous transactions");
			logger.info("----------------------------");
			logger.info("Balances before transfers:");
			logger.info("");
			logger.info(String.format("M. Johnson - %,.2f %s", mj_checking.getBalance(), mj_checking.getCurrency()));
			logger.info(String.format("B. Jones - %,.2f %s", bj_checking.getBalance(), bj_checking.getCurrency()));
			logger.info(String.format("D. Adams - %,.2f %s", da_checking.getBalance(), da_checking.getCurrency()));
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

			logger.info("Optimistic Locking active. Retries ongoing...");
			logger.info("");

			CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
			aRepo.flush();

			bj_checking = aRepo.findById(acc5.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
			mj_checking = aRepo.findById(acc9.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));
			da_checking = aRepo.findById(acc7.getId()).orElseThrow(() -> new EntityNotFoundException("Account not found"));

			BigDecimal mj_checkingBal = mj_checking.getBalance();
			bj_checkingBal = bj_checking.getBalance();
			da_checkingBal = da_checking.getBalance();

			logger.info("Balances after transfers:");
			logger.info("");
			logger.info(String.format("M. Johnson - %,.2f %s", mj_checkingBal, mj_checking.getCurrency()));
			logger.info(String.format("B. Jones - %,.2f %s", bj_checkingBal, bj_checking.getCurrency()));
			logger.info(String.format("D. Adams - %,.2f %s", da_checkingBal, da_checking.getCurrency()));
			if (mj_checkingBal.compareTo(new BigDecimal("15340.11")) == 0
			&& bj_checkingBal.compareTo(new BigDecimal("1500.00")) == 0
			&& da_checkingBal.compareTo(new BigDecimal("2000.00")) == 0) {
				logger.info("***********************************");
				logger.info("Concurrent transactions successful.");
				logger.info("***********************************");
				logger.info("Total retries: " + retryListener.getAndResetRetries());
				
			}
		};
	}

}

package clean;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableRetry
public class S1Application {

	private static final Logger logger = LoggerFactory.getLogger(S1Application.class);

	public static void main(String[] args) {
		SpringApplication.run(S1Application.class, args);
	}

	@Bean
	public CommandLineRunner demo(UserRepo uRepo, AccountRepo aRepo, TransferService service) {
		return (args) -> {

			User user1 = new User("John", "Smith");
			User user2 = new User("Bernard", "Jones");
			User user3 = new User("Deborah", "Adams");

			Account acc1 = new Account("Savings", new BigDecimal("9121.45"), "GBP");
			Account acc2 = new Account("Checking", new BigDecimal("2500.00"), "GBP");

			acc1.setUser(user1);
			acc2.setUser(user1);
			user1.addAccount(acc1);
			user1.addAccount(acc2);

			Account acc3 = new Account("Investment", new BigDecimal("90956.02"), "USD");
			Account acc4 = new Account("Savings", new BigDecimal("15643.98"), "USD");
			Account acc5 = new Account("Checking", new BigDecimal("6500.00"), "USD");
		
			acc3.setUser(user2);
			acc4.setUser(user2);
			acc5.setUser(user2);
			user2.addAccount(acc3);
			user2.addAccount(acc4);
			user2.addAccount(acc5);
			
			Account acc6 = new Account("Savings", new BigDecimal("12255.68"), "USD");
			Account acc7 = new Account("Checking", new BigDecimal("3000.00"), "USD");

			acc6.setUser(user3);
			acc7.setUser(user3);
			user3.addAccount(acc6);
			user3.addAccount(acc7);

			uRepo.save(user1);
			uRepo.save(user2);
			uRepo.save(user3);
			aRepo.save(acc1);
			aRepo.save(acc2);
			aRepo.save(acc3);
			aRepo.save(acc4);
			aRepo.save(acc5);
			aRepo.save(acc6);
			aRepo.save(acc7);

			logger.info("Users found with findAll():");
			logger.info("---------------------------------------");
			uRepo.findAll().forEach(user -> {
				logger.info(user.toString());
			});
			logger.info("");

			logger.info("Accounts found with findByLastName('Smith'):");
			logger.info("---------------------------------------");
			aRepo.findByUserLastName("Smith").forEach(smith -> {
				logger.info(smith.toString());
			});
			logger.info("");

			logger.info("Accounts found with findByCurrency('USD'):");
			logger.info("------------------------------------------");
			aRepo.findByCurrency("USD").forEach(usd -> {
				logger.info(usd.toString());
			});
			logger.info("");
			
			BigDecimal beforeTransferOut = aRepo.findById(acc5.getId()).get().getBalance();
			BigDecimal beforeTransferIn = aRepo.findById(acc7.getId()).get().getBalance();
			String currencyOut = aRepo.findById(acc5.getId()).get().getCurrency();
			String currencyIn = aRepo.findById(acc7.getId()).get().getCurrency();	
			try {
				logger.info("B. Jones transfer 1,000.00 USD to D. Adams"); 
				logger.info("------------------------------------------");	
				logger.info(String.format("Balances before transfer: B. Jones - %,.2f %s, D. Adams - %,.2f %s",
				beforeTransferOut, currencyOut, beforeTransferIn, currencyIn));			
				service.transferMoney(acc7.getId(), acc5.getId(), new BigDecimal("1000.00"));				
				logger.info("");
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
			}

			BigDecimal afterTransferOut = aRepo.findById(acc5.getId()).get().getBalance();
			BigDecimal afterTransferIn = aRepo.findById(acc7.getId()).get().getBalance();
			logger.info(String.format("Balances after transfer: B. Jones - %,.2f %s, D. Adams - %,.2f %s",
			afterTransferOut, currencyOut, afterTransferIn, currencyIn));
			if (afterTransferOut.compareTo(new BigDecimal("5500.00")) == 0
			&& afterTransferIn.compareTo(new BigDecimal("4000.00")) == 0) {
				logger.info("************************");
				logger.info("Transaction successful.");
				logger.info("************************");
			}

			BigDecimal beforeTransferFrom = aRepo.findById(acc7.getId()).get().getBalance();
			BigDecimal beforeTransferTo = aRepo.findById(acc5.getId()).get().getBalance();
			try {
				logger.info("D. Adams transfer 6,000.00 USD to B. Jones"); 
				logger.info("------------------------------------------");	
				logger.info(String.format("Balances before transfer: D. Adams - %,.2f %s, B. Jones - %,.2f %s",
				beforeTransferFrom, currencyOut, beforeTransferTo, currencyIn));		
				service.transferMoney(acc7.getId(), acc5.getId(), new BigDecimal("6000.00"));				
				logger.info("");
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
			}

			BigDecimal afterTransferFrom = aRepo.findById(acc7.getId()).get().getBalance();
			BigDecimal afterTransferTo = aRepo.findById(acc5.getId()).get().getBalance();
			logger.info(String.format("Balances after transfer: B. Jones - %,.2f %s, D. Adams - %,.2f %s",
			afterTransferFrom, currencyOut, afterTransferTo, currencyIn));

			if (beforeTransferFrom.compareTo(afterTransferFrom) == 0
			&& beforeTransferTo.compareTo(afterTransferTo) == 0) {
				logger.info("*********************");
				logger.info("Rollback successful.");
				logger.info("*********************");
			}
		};
	}

}

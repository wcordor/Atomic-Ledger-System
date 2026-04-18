package clean;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class S1Application {

	private static final Logger logger = LoggerFactory.getLogger(S1Application.class);

	public static void main(String[] args) {
		SpringApplication.run(S1Application.class, args);
	}

	@Bean
	public CommandLineRunner demo(UserRepo uRepo, AccountRepo aRepo, TransferService service) {
		return (args) -> {

			User user1 = new User("Cristiano", "Ronaldo");
			User user2 = new User("LeBron", "James");
			User user3 = new User("Terence", "Crawford");

			Account acc1 = new Account("Savings", new BigDecimal("778675419.92"), "SAR");
			Account acc2 = new Account("Checking", new BigDecimal("187625888.93"), "SAR");

			acc1.setUser(user1);
			acc2.setUser(user1);
			user1.addAccount(acc1);
			user1.addAccount(acc2);

			Account acc3 = new Account("Investment", new BigDecimal("752998229.30"), "USD");
			Account acc4 = new Account("Savings", new BigDecimal("330123953.55"), "USD");
			Account acc5 = new Account("Checking", new BigDecimal("101293620.49"), "USD");
		
			acc3.setUser(user2);
			acc4.setUser(user2);
			acc5.setUser(user2);
			user2.addAccount(acc3);
			user2.addAccount(acc4);
			user2.addAccount(acc5);
			
			Account acc6 = new Account("Savings", new BigDecimal("41127855.16"), "USD");
			Account acc7 = new Account("Checking", new BigDecimal("10355628.89"), "USD");

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

			logger.info("Accounts found with findByLastName('Ronaldo'):");
			logger.info("---------------------------------------");
			aRepo.findByUserLastName("Ronaldo").forEach(cr7 -> {
				logger.info(cr7.toString());
			});
			logger.info("");

			logger.info("Accounts found with findByCurrency('USD'):");
			logger.info("------------------------------------------");
			aRepo.findByCurrency("USD").forEach(usd -> {
				logger.info(usd.toString());
			});
			logger.info("");
			
			BigDecimal beforeTransfer = aRepo.findById(acc7.getId()).get().getBalance();			
			try {
				logger.info("Money Transfer"); // to be updated
				logger.info("------------------------------------------");	
				logger.info("Balance before transfer: {}", beforeTransfer);			
				service.transferMoney(acc1, acc7, new BigDecimal("752998229.30"));				
				logger.info("");
			} catch (InsufficientFundsException e) {
				logger.error("ERROR: " + e.getMessage());
			}

			BigDecimal afterTransfer = aRepo.findById(acc7.getId()).get().getBalance();
			BigDecimal inBalance = aRepo.findById(acc1.getId()).get().getBalance();
			logger.info("Verification of rollback: Acc1 balance - " + inBalance + ", Acc7 balance - " + afterTransfer);

			if (beforeTransfer.equals(afterTransfer)) {
				logger.info("ATOMICITY PROVEN: Rollback successfull.");
			}
		};
	}

}

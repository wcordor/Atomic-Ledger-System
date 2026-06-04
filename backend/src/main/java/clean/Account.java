package clean;

import java.math.BigDecimal;

import jakarta.persistence.*;

@Entity
public class Account {
    
    private BigDecimal balance;
    private String currency;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private Long id;

    private String name;

    @Version
    private Integer version;

    protected Account() {}

    public Account(String name, BigDecimal balance, String currency) {
        this.name = name;
        this.balance = balance;
        this.currency = currency;
    }

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @Override
    public String toString() {
        String userName = (user != null) ? user.getFirstName() + " " + user.getLastName() : "No Owner";
        return String.format("Account[name=%s, id=%d, user=%s, balance=%,.2f %s]", name, id, userName,
        balance, currency);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}

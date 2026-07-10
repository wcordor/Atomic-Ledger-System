package ledger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "accounts")
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

    public Account(String name, BigDecimal balance, String currency/*, User user */) {
        this.name = name;
        this.balance = balance;
        this.currency = currency;
    }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "sender", cascade = CascadeType.MERGE, orphanRemoval = true)
    private List<Transaction> sent = new ArrayList<>();

    @OneToMany(mappedBy = "receiver", cascade = CascadeType.MERGE, orphanRemoval = true)
    private List<Transaction> received = new ArrayList<>();

    @Override
    public String toString() {
        String userName = (user != null) ? user.getFirstName() + " " + user.getLastName() : "No Owner";
        return String.format("Account[name=%s, id=%d, owner=%s, balance=%,.2f %s]", name, id, userName,
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

    public void addTransaction(Transaction transaction) {
        if (transaction.getSender() == this) {
            sent.add(transaction);
        } else if (transaction.getReceiver() == this) {
            received.add(transaction);
        }
    }

    public List<Transaction> getTransactions() {
        List<Transaction> transactions = new ArrayList<>(sent);
        transactions.addAll(received);
        return transactions;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        result = prime * result + ((version == null) ? 0 : version.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Account other = (Account) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        if (version == null) {
            if (other.version != null)
                return false;
        } else if (!version.equals(other.version))
            return false;
        return true;
    }

}

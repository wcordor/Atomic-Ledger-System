package ledger;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "transaction_seq")
    @SequenceGenerator(name = "transaction_seq", sequenceName = "transaction_seq", allocationSize = 50) // check for any potential issues with allocationSize
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id")
    private Account sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private Account receiver;

    private BigDecimal amount;

    @Column(nullable = false)
    private Instant timestamp;

    private String currency;

    private Status status;

    protected Transaction() {}

    public Transaction(Account receiver, Account sender, BigDecimal amount, String currency, Status status) {
        this.sender = sender;
        this.receiver = receiver;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.timestamp = Instant.now();
    }

    @Override
    public String toString() {
        return String.format("Transaction[id=%d, to %s, from %s, amount=%,.2f %s, status=%s, timestamp=%s]",
            id, receiver.getUser().getFirstName() + " " + receiver.getUser().getLastName(),
            sender.getUser().getFirstName() + " " + sender.getUser().getLastName(),
            amount, currency, status, timestamp);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        if (this.id == null) {
            this.id = id;
        }
    }

    public void setSender(Account sender) {
        if (this.sender == null) {
            this.sender = sender;
        }
    }

    public void setReceiver(Account receiver) {
        if (this.receiver == null) {
            this.receiver = receiver;
        }
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        if (this.amount == null) {
            this.amount = amount;
        }
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        if (this.currency == null) {
            this.currency = currency;
        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        if (this.status != Status.SUCCESSFUL && this.status != Status.FAILED) {
            this.status = status;
        }
    }

    public Long getSenderId() {
        return sender.getId();
    }

    public Long getReceiverId() {
        return receiver.getId();
    }

    public List<Account> getAccounts() {
        List<Account> accounts = new ArrayList<>();
        if (sender != null) {
            accounts.add(sender);
        }
        if (receiver != null) {
            accounts.add(receiver);
        }
        return accounts;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
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
        Transaction other = (Transaction) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
    
}

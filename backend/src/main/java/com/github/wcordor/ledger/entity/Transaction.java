package com.github.wcordor.ledger.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.wcordor.ledger.Status;

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

    @JsonIgnore
    private Status status;

    @JsonIgnore
    private List<Long> accountIds = new ArrayList<>();

    protected Transaction() {}

    public Transaction(Account receiver, Account sender, BigDecimal amount, String currency) {
        this.amount = amount;
        this.currency = currency;
        this.timestamp = Instant.now();
        this.sender = sender;
        this.receiver = receiver;

        this.sender.addTransaction(this);
        this.receiver.addTransaction(this);
    }

    @Override
    public String toString() {
        return String.format("Transaction[ID: %d, Receiver ID: %d, Sender ID: %d, Amount: %,.2f %s, Timestamp: %s]",
            id, receiver.getId(), sender.getId(), amount, currency, timestamp);
    }

    public Long getId() {
        return id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public Status getStatus() {
        return status;
    }

    public Long getSenderId() {
        return sender.getId();
    }

    public Long getReceiverId() {
        return receiver.getId();
    }

    public List<Long> getAccountIds() {
        if (sender != null) {
            accountIds.add(sender.getId());
        }
        if (receiver != null) {
            accountIds.add(receiver.getId());
        }
        return accountIds;
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

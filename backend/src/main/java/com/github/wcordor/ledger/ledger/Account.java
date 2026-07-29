package com.github.wcordor.ledger.ledger;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.wcordor.ledger.Transaction;
import com.github.wcordor.ledger.User;

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

    Account(User user, String name, BigDecimal initialDeposit, String currency) {
        this.name = name;
        balance = initialDeposit;
        this.currency = currency;
        this.user = user;
        this.user.addAccount(this);
    }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "sender", orphanRemoval = true)
    private Set<Transaction> sent = new HashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "receiver", orphanRemoval = true)
    private Set<Transaction> received = new HashSet<>();

    @Override
    public String toString() {
        String userName = (user != null) ? user.getFirstName() + " " + user.getLastName() : "No Owner";
        return String.format("Account[name=%s, id=%d, owner=%s, balance=%,.2f %s]", name, id, userName,
        balance, currency);
    }

    public Long getId() {
        return id;
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void addTransaction(Transaction transaction) {
        if (transaction.getSenderId() == this.id) {
            sent.add(transaction);
        } else if (transaction.getReceiverId() == this.id) {
            received.add(transaction);
        }
    }

    public List<Transaction> getTransactions() {
        List<Transaction> transactions = new ArrayList<>(sent);
        transactions.addAll(received);
        return transactions;
    }

    public Set<Transaction> getSent() {
        return sent;
    }

    public Set<Transaction> getReceived() {
        return received;
    }

    public Long getUserId() {
        return user.getId();
    }

    public void setUser(User user) {
        if (this.user == null) {
            this.user = user;
        }
    }

    void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    void debit(BigDecimal amount) {
        balance = balance.subtract(amount);
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

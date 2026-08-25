package com.github.wcordor.ledger.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    protected Account() {}

    public Account(User user, String name, BigDecimal initialDeposit, String currency) {
        this.name = name;
        balance = initialDeposit;
        this.currency = currency;
        this.user = user;
        this.user.addAccount(this);
    }

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "sender", orphanRemoval = true)
    private Set<Transaction> sent = new HashSet<>();

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

    public String getUserName() {
        return user.getName();
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

    public void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        balance = balance.subtract(amount);
    }

    public String getInfo() {
        return String.format("name: %s, id: %d, balance: %,.2f %s", name, id, balance, currency);
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
        Account other = (Account) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }

}

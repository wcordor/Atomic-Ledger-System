package com.github.wcordor.ledger;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class IdempotencyKey {
    
    @Id
    private String key;
    private LocalDateTime expiryDate;

    protected IdempotencyKey() {}

    public IdempotencyKey(String key, LocalDateTime expiryDate) {
        this.key = key;
        this.expiryDate = expiryDate;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public LocalDateTime getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDateTime expiryDate) {
        this.expiryDate = expiryDate;
    }

}

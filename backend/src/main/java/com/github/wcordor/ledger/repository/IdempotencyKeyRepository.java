package com.github.wcordor.ledger.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.wcordor.ledger.entity.IdempotencyKey;

@Repository
public interface IdempotencyKeyRepository extends JpaRepository<IdempotencyKey, Long> {
    
    Optional<IdempotencyKey> findByKey(String key);
}

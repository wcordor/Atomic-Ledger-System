package com.github.wcordor.ledger.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.wcordor.ledger.entity.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}

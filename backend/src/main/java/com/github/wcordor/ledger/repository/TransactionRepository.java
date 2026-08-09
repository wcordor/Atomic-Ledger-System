package com.github.wcordor.ledger.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.wcordor.ledger.entity.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

}

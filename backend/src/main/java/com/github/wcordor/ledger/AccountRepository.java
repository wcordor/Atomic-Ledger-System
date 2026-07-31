package com.github.wcordor.ledger;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.wcordor.ledger.ledger.Account;

import jakarta.persistence.LockModeType;

public interface AccountRepository extends JpaRepository<Account, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Account> findWithLockingById(Long id);

    List<Account> findByName(String name);
    List<Account> findByUserLastName(String lastName);
    List<Account> findByCurrency(String currency);

    @EntityGraph(attributePaths = {"sent", "received"})
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findById(@Param("id") Long id);
    
    List<Account> findByUser_Id(Long id);
    Optional<Account> findByIdAndUser_Id(Long accountId, Long userId);
}

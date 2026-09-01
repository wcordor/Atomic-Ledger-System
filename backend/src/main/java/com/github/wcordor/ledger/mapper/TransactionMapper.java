package com.github.wcordor.ledger.mapper;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.stereotype.Component;

import com.github.wcordor.ledger.dtos.transactionDTO.TransactionResponseDTO;
import com.github.wcordor.ledger.entity.Transaction;

@Component
public class TransactionMapper {

    public TransactionResponseDTO toDTO(Transaction transaction) {
        Long senderId = transaction.getSenderId();
        Long receiverId = transaction.getReceiverId();
        BigDecimal amount = transaction.getAmount();
        String currency = transaction.getCurrency();
        Instant timestamp = transaction.getTimestamp();
        Long id = transaction.getId();

        return new TransactionResponseDTO(senderId, receiverId, amount, currency, timestamp, id);
    }
}

package com.github.wcordor.ledger.dtos.transactionDTO;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponseDTO(

    Long senderId,
    Long receiverId,
    BigDecimal amount,
    String currency,
    Instant timestamp
) {}

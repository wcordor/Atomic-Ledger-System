package com.github.wcordor.ledger.dtos.transactionDTO;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransactionCreationDTO(
    
    @NotNull
    Long receiverId,
    
    @NotNull
    BigDecimal amount,

    @NotBlank
    String currency

) {}

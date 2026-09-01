package com.github.wcordor.ledger.dtos.transactionDTO;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record TransactionCreationDTO(
    
    @NotNull(message = "Receiver Id must not be null.")
    Long receiverId,
    
    @NotNull(message = "Transfer amount must not be null.")
    @Positive(message = "Transfer amount must be greater than 0.")
    BigDecimal amount,

    @NotBlank(message = "Please add currency.")
    String currency

) {}

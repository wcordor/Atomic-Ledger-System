package com.github.wcordor.ledger.dtos.accountDTO;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountCreationDTO(
    
    @NotBlank
    String name,

    @NotNull
    BigDecimal initialDeposit,

    @NotBlank
    String currency,

    @NotNull
    Long userId
) {}

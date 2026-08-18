package com.github.wcordor.ledger.dtos.accountDTO;

import java.math.BigDecimal;

import com.github.wcordor.ledger.entity.User;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AccountCreationDTO(
    
    @NotBlank
    String name,

    @NotNull
    BigDecimal initialDeposit,

    @NotBlank
    String currency
) {
    
}

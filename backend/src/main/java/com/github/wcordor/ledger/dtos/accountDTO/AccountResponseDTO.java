package com.github.wcordor.ledger.dtos.accountDTO;

import java.math.BigDecimal;
import java.util.List;

public record AccountResponseDTO(

    String name,
    BigDecimal balance,
    String currency,
    String userName,
    List<String> transactions

) {}

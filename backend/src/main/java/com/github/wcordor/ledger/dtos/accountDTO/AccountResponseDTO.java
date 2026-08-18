package com.github.wcordor.ledger.dtos.accountDTO;

import java.math.BigDecimal;
import java.util.List;

import com.github.wcordor.ledger.entity.Transaction;
import com.github.wcordor.ledger.entity.User;

public record AccountResponseDTO(

    String name,
    BigDecimal balance,
    String currency,
    String userName,
    List<String> transactions

) {
    
}

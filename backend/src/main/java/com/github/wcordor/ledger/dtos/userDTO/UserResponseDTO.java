package com.github.wcordor.ledger.dtos.userDTO;

import java.util.List;

public record UserResponseDTO(

    String firstName, 
    String lastName, 
    List<String> accounts,
    Long id
    
) {}

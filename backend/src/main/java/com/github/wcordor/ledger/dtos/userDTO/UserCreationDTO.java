package com.github.wcordor.ledger.dtos.userDTO;

import jakarta.validation.constraints.NotBlank;

public record UserCreationDTO(

    @NotBlank
    String firstName,
    
    @NotBlank
    String lastName

) {}

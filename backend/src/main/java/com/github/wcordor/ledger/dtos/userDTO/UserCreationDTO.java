package com.github.wcordor.ledger.dtos.userDTO;

import jakarta.validation.constraints.NotBlank;

public record UserCreationDTO(

    @NotBlank(message = "First name must not be blank.")
    String firstName,
    
    @NotBlank(message = "Last name must not be blank.")
    String lastName

) {}

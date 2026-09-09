package com.github.wcordor.ledger.dtos.userDTO;

import jakarta.validation.constraints.NotBlank;

public record UserCreationDTO(

    @NotBlank(message = "First name must not be blank.")
    String firstName,
    
    @NotBlank(message = "Last name must not be blank.")
    String lastName,

    @NotBlank(message = "Username must not be blank.")
    String username,

    @NotBlank(message = "Password must not be blank.")
    String password

) {}

package com.github.wcordor.ledger.dtos.userDTO;

import java.util.List;

import jakarta.validation.constraints.NotBlank;

public record UserCreationDTO(

    @NotBlank
    String firstName,
    
    @NotBlank
    String lastName

) {
    
    /*private String firstName;
    private String lastName;
    private List<String> accounts;

    public UserCreationDTO(String firstName, String lastName, List<String> accounts) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.accounts = accounts;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public List<String> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<String> accounts) {
        this.accounts = accounts;
    }*/

}

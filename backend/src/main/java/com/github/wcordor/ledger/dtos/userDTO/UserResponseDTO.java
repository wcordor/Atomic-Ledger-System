package com.github.wcordor.ledger.dtos.userDTO;

import java.util.List;

import com.github.wcordor.ledger.entity.Transaction;

//import com.github.wcordor.ledger.entity.Account;

public record UserResponseDTO(

    String firstName, 
    String lastName, 
    List<String> accounts
    
) {
    
    /*private String firstName;
    private String lastName;
    private List<String> accounts;

    public UserResponseDTO(String firstName, String lastName, List<String> accounts) {
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

package com.github.wcordor.ledger.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.github.wcordor.ledger.dtos.userDTO.UserResponseDTO;
import com.github.wcordor.ledger.dtos.userDTO.UserCreationDTO;
import com.github.wcordor.ledger.entity.Account;
import com.github.wcordor.ledger.entity.User;

@Component
public class UserMapper {
    
    public UserResponseDTO toDTO(User user) {
        String firstName = user.getFirstName();
        String lastName = user.getLastName();
        
        @SuppressWarnings("null")
        List<String> accounts = user.getAccounts()
            .stream().map(Account::getInfo).toList();

        Long id = user.getId();
        
        return new UserResponseDTO(firstName, lastName, accounts, id);
    }

    public User toUser(UserCreationDTO userDTO) {
        return new User(userDTO.firstName(), userDTO.lastName());
    }
}

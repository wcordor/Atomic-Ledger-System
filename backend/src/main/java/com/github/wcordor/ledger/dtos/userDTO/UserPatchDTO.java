package com.github.wcordor.ledger.dtos.userDTO;

import org.openapitools.jackson.nullable.JsonNullable;

public class UserPatchDTO {

    private JsonNullable<String> firstName = JsonNullable.undefined();
    private JsonNullable<String> lastName = JsonNullable.undefined();

    public UserPatchDTO(JsonNullable<String> firstName, JsonNullable<String> lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public JsonNullable<String> getFirstName() {
        return firstName;
    }

    public void setFirstName(JsonNullable<String> firstName) {
        this.firstName = firstName;
    }

    public JsonNullable<String> getLastName() {
        return lastName;
    }

    public void setLastName(JsonNullable<String> lastName) {
        this.lastName = lastName;
    }
    
}

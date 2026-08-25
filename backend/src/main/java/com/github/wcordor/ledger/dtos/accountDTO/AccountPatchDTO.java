package com.github.wcordor.ledger.dtos.accountDTO;

import org.openapitools.jackson.nullable.JsonNullable;

public class AccountPatchDTO {
    
    private JsonNullable<String> name = JsonNullable.undefined();

    public AccountPatchDTO(JsonNullable<String> name) {
        this.name = name;
    }

    public JsonNullable<String> getName() {
        return name;
    }

    public void setName(JsonNullable<String> name) {
        this.name = name;
    }
}

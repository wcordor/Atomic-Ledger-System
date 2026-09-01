package com.github.wcordor.ledger.exception;

public class InvalidTransferException extends RuntimeException {
    
    public InvalidTransferException() {
        super("Account receiving funds must not be the account sending the funds.");
    }
}

package com.github.wcordor.ledger.exception;

public class InvalidUserIdException extends RuntimeException {
    
    public InvalidUserIdException() {
        super("User ID does not match ID of Account Owner.");
    }
}

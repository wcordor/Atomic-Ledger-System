package com.github.wcordor.ledger.exception;

public class NullUserNameException extends RuntimeException {
    
    public NullUserNameException() {
        super("User's first and last name must not be null");
    }
}

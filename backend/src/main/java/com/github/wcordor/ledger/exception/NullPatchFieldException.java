package com.github.wcordor.ledger.exception;

public class NullPatchFieldException extends RuntimeException {
    
    public NullPatchFieldException() {
        super("The field(s) selected for change must not be null.");
    }
}

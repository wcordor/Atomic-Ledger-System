package com.github.wcordor.ledger.exception;

public class IdempotencyKeyAlreadyExistsException extends RuntimeException {

    public IdempotencyKeyAlreadyExistsException() {
		super("Key already exists.");
	}
}

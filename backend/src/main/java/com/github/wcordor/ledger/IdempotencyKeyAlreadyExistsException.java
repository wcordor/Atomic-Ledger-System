package com.github.wcordor.ledger;

public class IdempotencyKeyAlreadyExistsException extends RuntimeException {

    public IdempotencyKeyAlreadyExistsException() {
		super("Key already exists.");
	}
}

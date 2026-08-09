package com.github.wcordor.ledger.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long accountId, Long userId) {
		super("Account " + accountId + " either does not exist, or does not belong to User " + userId + ".");
	}

}

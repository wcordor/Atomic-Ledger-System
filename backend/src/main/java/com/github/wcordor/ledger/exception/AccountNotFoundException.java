package com.github.wcordor.ledger.exception;

public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long accountId, Long userId) {
		super("Account " + accountId + " and/or User " + userId + " may not exist, or Account " + accountId
		+ " does not belong to User " + userId + ".");
	}

}

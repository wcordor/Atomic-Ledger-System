package com.github.wcordor.ledger;

public class AccountDeletionFailureException extends RuntimeException {

    public AccountDeletionFailureException(Long accountId, Long userId) {
		super("Account " + accountId + " can't be deleted because it may not exist, belong to User " + userId + ", or have an empty balance.");
	}
}

package com.github.wcordor.ledger;

public class UserDeletionFailureException extends RuntimeException {

    public UserDeletionFailureException(Long id) {
		super("User " + id + " could not be deleted as it still has accounts open.");
	}
}

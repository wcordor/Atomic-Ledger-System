package com.github.wcordor.ledger;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class AccountDeletionFailureAdvice {
    
    @ExceptionHandler(AccountDeletionFailureException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public String accountNotDeletedHandler(AccountDeletionFailureException e) {
		return e.getMessage();
	}
}

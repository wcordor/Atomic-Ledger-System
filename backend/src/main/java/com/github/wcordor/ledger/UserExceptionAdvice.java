package com.github.wcordor.ledger;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserExceptionAdvice {
    
    @ExceptionHandler(UserNotFoundException.class)
	@ResponseStatus(HttpStatus.NOT_FOUND)
	public String userNotFoundHandler(UserNotFoundException e) {
		return e.getMessage();
	}

	@ExceptionHandler(UserDeletionFailureException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public String userDeletionFailureHandler(UserDeletionFailureException e) {
		return e.getMessage();
	}
}

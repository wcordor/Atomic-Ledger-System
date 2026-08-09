package com.github.wcordor.ledger.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.github.wcordor.ledger.exception.IdempotencyKeyAlreadyExistsException;

@RestControllerAdvice
public class IdempotencyKeyAlreadyExistsAdvice {
    
    @ExceptionHandler(IdempotencyKeyAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String accountNotDeletedHandler(IdempotencyKeyAlreadyExistsException e) {
        return e.getMessage();
    }
}

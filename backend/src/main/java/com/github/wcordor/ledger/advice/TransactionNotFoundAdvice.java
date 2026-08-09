package com.github.wcordor.ledger.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.github.wcordor.ledger.exception.TransactionNotFoundException;

@RestControllerAdvice
public class TransactionNotFoundAdvice {
    @ExceptionHandler(TransactionNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String transactionNotFoundHandler(TransactionNotFoundException e) {
        return e.getMessage();
    }
}

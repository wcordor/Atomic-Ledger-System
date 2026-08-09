package com.github.wcordor.ledger.advice;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.github.wcordor.ledger.exception.InsufficientFundsException;

@RestControllerAdvice
public class InsufficientFundsAdvice {
    
    @ExceptionHandler(InsufficientFundsException.class)
    @ResponseStatus(HttpStatus.PAYMENT_REQUIRED)
    public String insufficientFundsHandler(InsufficientFundsException e) {
        return e.getMessage();
    }
}

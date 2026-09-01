package com.github.wcordor.ledger.advice;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.github.wcordor.ledger.exception.IdempotencyKeyAlreadyExistsException;
import com.github.wcordor.ledger.exception.NullPatchFieldException;

@RestControllerAdvice
public class MiscellaneousExceptionAdvice {
    
    @ExceptionHandler(IdempotencyKeyAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public String idempotencyKeyAlreadyExistsHandler(IdempotencyKeyAlreadyExistsException e) {
        return e.getMessage();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> methodArgumentNotValidHandler(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
        .map(err -> err.getDefaultMessage())
        .collect(Collectors.joining("\n"));

    	return ResponseEntity.badRequest().body(message);
    }

	@ExceptionHandler(NullPatchFieldException.class)
	@ResponseStatus(HttpStatus.CONFLICT)
	public String nullPatchFieldHandler(NullPatchFieldException e) {
		return e.getMessage();
	}
}

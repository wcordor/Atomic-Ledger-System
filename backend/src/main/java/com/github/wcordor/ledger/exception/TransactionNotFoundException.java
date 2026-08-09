package com.github.wcordor.ledger.exception;

public class TransactionNotFoundException extends RuntimeException {

    public TransactionNotFoundException(Long transactionId, Long accountId) {
        super("Transaction " + transactionId + " either does not exist or does not belong to account " + accountId + ".");
    }

}

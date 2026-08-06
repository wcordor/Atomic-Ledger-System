package com.github.wcordor.ledger;

public class InsufficientFundsException extends RuntimeException {
    public InsufficientFundsException() {
        super("Not enough funds to make transaction, canceling transaction.");
    }
}

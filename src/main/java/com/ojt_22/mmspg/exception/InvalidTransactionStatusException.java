package com.ojt_22.mmspg.exception;

public class InvalidTransactionStatusException extends RuntimeException {

    public InvalidTransactionStatusException(String message) {
        super(message);
    }
}
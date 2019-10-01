package com.example.muchbetter;

/**
 * Simple exception to indicate the user doesn't have enough balance to apply a transaction.
 */
public class InsifficientFundsException extends RuntimeException {

    public InsifficientFundsException(String message) {
        super(message);
    }

}

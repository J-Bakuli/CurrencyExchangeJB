package com.jb.currencyexchange.exception.creation;

public class CurrencyCreationFailedException extends RuntimeException {
    public CurrencyCreationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}
package com.jb.currencyexchange.exception.alreadyexists;

public class CurrencyAlreadyExistsException extends AlreadyExistsException {
    public CurrencyAlreadyExistsException(String message) {
        super(message);
    }

    public CurrencyAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

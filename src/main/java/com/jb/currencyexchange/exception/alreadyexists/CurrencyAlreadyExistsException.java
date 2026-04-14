package com.jb.currencyexchange.exception.alreadyexists;

import com.jb.currencyexchange.exception.AlreadyExistsException;

public class CurrencyAlreadyExistsException extends AlreadyExistsException {
    public CurrencyAlreadyExistsException(String message) {
        super(message);
    }

    public CurrencyAlreadyExistsException(String message, Throwable cause) {
        super(message, cause);
    }
}

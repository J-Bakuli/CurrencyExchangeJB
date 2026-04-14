package com.jb.currencyexchange.exception.validation;

import com.jb.currencyexchange.exception.ValidationException;

public class CurrencyValidationException extends ValidationException {
    public CurrencyValidationException(String message) {
        super(message, null);
    }
}

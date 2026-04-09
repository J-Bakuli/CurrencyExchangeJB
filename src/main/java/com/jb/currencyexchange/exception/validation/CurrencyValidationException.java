package com.jb.currencyexchange.exception.validation;

public class CurrencyValidationException extends ValidationException{
    public CurrencyValidationException(String message) {
        super(message, null);
    }
}

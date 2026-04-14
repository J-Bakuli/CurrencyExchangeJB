package com.jb.currencyexchange.exception.validation;

import com.jb.currencyexchange.exception.ValidationException;

public class ExchangeRateIntergityException extends ValidationException {
    public ExchangeRateIntergityException(String message, Throwable cause) {
        super(message, cause);
    }
}

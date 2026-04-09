package com.jb.currencyexchange.exception.validation;

public class ExchangeRateIntergityException extends ValidationException{
    public ExchangeRateIntergityException(String message, Throwable cause) {
        super(message, String.valueOf(cause));
    }
}

package com.jb.currencyexchange.exception.update;

public class ExchangeRateUpdateException extends UpdateFailedException {
    public ExchangeRateUpdateException(String message, Throwable cause) {
        super(message, cause);
    }
}

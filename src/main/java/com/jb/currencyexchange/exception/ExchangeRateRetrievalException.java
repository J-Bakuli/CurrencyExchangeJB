package com.jb.currencyexchange.exception;

public class ExchangeRateRetrievalException extends RuntimeException {
    public ExchangeRateRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}

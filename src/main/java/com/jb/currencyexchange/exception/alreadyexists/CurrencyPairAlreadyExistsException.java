package com.jb.currencyexchange.exception.alreadyexists;

import lombok.Getter;

public class CurrencyPairAlreadyExistsException extends RuntimeException {
    @Getter
    private final String baseCurrencyCode;

    @Getter
    private final String targetCurrencyCode;

    public CurrencyPairAlreadyExistsException(String message) {
        super(message);
        this.baseCurrencyCode = null;
        this.targetCurrencyCode = null;
    }

    public CurrencyPairAlreadyExistsException(String baseCurrencyCode, String targetCurrencyCode) {
        super(String.format("Currency pair %s-%s already exists", baseCurrencyCode, targetCurrencyCode));
        this.baseCurrencyCode = baseCurrencyCode;
        this.targetCurrencyCode = targetCurrencyCode;
    }
}

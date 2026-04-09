package com.jb.currencyexchange.exception.notfound;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class CurrencyNotFoundException extends RuntimeException {
    private final String baseCurrencyCode;
    private final String targetCurrencyCode;
    private final List<String> missingCurrencies;

    public CurrencyNotFoundException(String baseCurrencyCode, String targetCurrencyCode, List<String> missingCurrencies) {
        super(createErrorMessage(baseCurrencyCode, targetCurrencyCode, missingCurrencies));
        this.baseCurrencyCode = baseCurrencyCode;
        this.targetCurrencyCode = targetCurrencyCode;
        this.missingCurrencies = List.copyOf(missingCurrencies);
    }

    private static String createErrorMessage(String baseCode, String targetCode, List<String> missingCurrencies) {
        if (missingCurrencies.isEmpty()) {
            return "Currency not found";
        }

        List<String> currencyDescriptions = new ArrayList<>();
        for (String code : missingCurrencies) {
            if (code.equals(baseCode)) {
                currencyDescriptions.add("base currency '" + code + "'");
            } else {
                currencyDescriptions.add("target currency '" + code + "'");
            }
        }

        return "Currency not found: " + String.join(" and ", currencyDescriptions);
    }
}
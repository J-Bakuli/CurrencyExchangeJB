package com.jb.currencyexchange.validation.structural;

import com.jb.currencyexchange.exception.ValidationException;
import com.jb.currencyexchange.util.ValidationUtils;

import java.math.BigDecimal;

import static com.jb.currencyexchange.util.ValidationUtils.*;

public class ExchangeRateValidation {
    public static void validateRateParams(String from, String to, String amountStr) {
        CurrencyValidation.validateCurrencyCode(from);
        CurrencyValidation.validateCurrencyCode(to);

        if (ValidationUtils.isBlank(amountStr)) {
            throw new ValidationException("Amount parameter is required");
        } else {
            try {
                BigDecimal amount = new BigDecimal(amountStr.trim());
                ValidationUtils.validateNumber(
                        amount, "amount", AMOUNT_MAX_DIGITS, AMOUNT_MAX_SCALE, true);
            } catch (NumberFormatException e) {
                throw new ValidationException(e.getMessage(), e);
            }
        }
    }

    public static void validateRateParams(String from, String to, BigDecimal rate) {
        CurrencyValidation.validateCurrencyCode(from);
        CurrencyValidation.validateCurrencyCode(to);
        try {
            ValidationUtils.validateNumber(
                    rate, "rate", RATE_MAX_INTEGER_DIGITS, RATE_MAX_FRACTION_DIGITS, false);
        } catch (NumberFormatException e) {
            throw new ValidationException(e.getMessage(), e);
        }
    }
}

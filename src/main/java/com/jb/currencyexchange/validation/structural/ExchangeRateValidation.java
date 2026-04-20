package com.jb.currencyexchange.validation.structural;

import com.jb.currencyexchange.exception.ValidationException;
import com.jb.currencyexchange.util.CommonValidationUtils;

import java.math.BigDecimal;

import static com.jb.currencyexchange.util.CommonValidationUtils.*;

public class ExchangeRateValidation {
    public static void validateRateParams(String from, String to, String amountStr) {
        CurrencyValidation.validateCurrencyCode(from);
        CurrencyValidation.validateCurrencyCode(to);

        if (CommonValidationUtils.isBlank(amountStr)) {
            throw new ValidationException("Amount parameter is required");
        } else {
            try {
                BigDecimal amount = new BigDecimal(amountStr.trim());
                CommonValidationUtils.validateNumber(
                        amount, "amount", AMOUNT_MAX_DIGITS, AMOUNT_MAX_SCALE, true);
            } catch (NumberFormatException e) {
                throw new ValidationException("Amount must be a valid number");
            }
        }
    }

    public static void validateRateParams(String from, String to, BigDecimal rate) {
        CurrencyValidation.validateCurrencyCode(from);
        CurrencyValidation.validateCurrencyCode(to);
        try {
            CommonValidationUtils.validateNumber(
                    rate, "rate", RATE_MAX_INTEGER_DIGITS, RATE_MAX_FRACTION_DIGITS, false);
        } catch (NumberFormatException e) {
            throw new ValidationException("Rate must be a valid number");
        }
    }
}

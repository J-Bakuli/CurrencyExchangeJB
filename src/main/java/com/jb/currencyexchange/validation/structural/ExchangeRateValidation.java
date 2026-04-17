package com.jb.currencyexchange.validation.structural;

import com.jb.currencyexchange.exception.ValidationException;
import com.jb.currencyexchange.model.Currency;
import com.jb.currencyexchange.model.ExchangeRate;
import com.jb.currencyexchange.util.CommonValidationUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.jb.currencyexchange.util.CommonValidationUtils.*;

public class ExchangeRateValidation {
    public static void validateExchangeParams(String from, String to, String amountStr) {
        List<String> errors = new ArrayList<>();
        CurrencyValidation.validateCurrencyCode(from);
        CurrencyValidation.validateCurrencyCode(to);

        if (isBlank(amountStr)) {
            errors.add("'amount' parameter is required");
        } else {
            try {
                BigDecimal amount = new BigDecimal(amountStr.trim());
                validateNumber(amount, errors);
            } catch (NumberFormatException e) {
                errors.add("'amount' must be a valid number");
            }
        }
        throwValidationExceptionIfErrors(errors);
    }

    public static void validate(ExchangeRate rate) {
        if (rate == null) {
            throw new ValidationException("ExchangeRate object cannot be null");
        }

        Currency baseCurrency = rate.getBaseCurrency();
        Currency targetCurrency = rate.getTargetCurrency();
        BigDecimal rateValue = rate.getRate();

        if (baseCurrency == null) {
            throw new ValidationException("BaseCode cannot be null");
        }
        String baseCode = baseCurrency.getCode();
        if (isBlank(baseCode)) {
            throw new ValidationException("BaseCode.code cannot be null or empty");
        } else if (!CODE_PATTERN.matcher(baseCode).matches()) {
            throw new ValidationException("BaseCode must be 3 uppercase Latin letters");
        }

        if (targetCurrency == null) {
            throw new ValidationException("TargetCode cannot be null");
        }
        String targetCode = targetCurrency.getCode();
        if (isBlank(targetCode)) {
            throw new ValidationException("TargetCode.code cannot be null or empty");
        } else if (!CODE_PATTERN.matcher(targetCode).matches()) {
            throw new ValidationException("TargetCode must be 3 uppercase Latin letters");
        }

        if (rateValue == null) {
            throw new ValidationException("Rate cannot be null");
        } else if (rateValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Rate must be positive");
        } else {
            int scale = rateValue.stripTrailingZeros().scale();
            int integerDigits = rateValue.precision() - Math.max(scale, 0);

            if (integerDigits > RATE_MAX_INTEGER_DIGITS) {
                throw new ValidationException("Rate must have at most " + RATE_MAX_INTEGER_DIGITS + " integer digits");
            }
            if (scale > RATE_MAX_FRACTION_DIGITS) {
                throw new ValidationException("Rate must have at most " + RATE_MAX_FRACTION_DIGITS + " fractional digits");
            }
        }
    }

    private static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    private static void validateNumber(BigDecimal number, List<String> errors) {
        int totalDigits = number.stripTrailingZeros().precision();
        int scale = number.stripTrailingZeros().scale();

        if (totalDigits > CommonValidationUtils.AMOUNT_MAX_DIGITS) {
            errors.add("'amount' must contain at most " + CommonValidationUtils.AMOUNT_MAX_DIGITS + " digits");
        }
        if (scale > com.jb.currencyexchange.util.CommonValidationUtils.AMOUNT_MAX_SCALE) {
            errors.add("'amount' must have at most " + CommonValidationUtils.AMOUNT_MAX_SCALE + " fractional digits");
        }
    }

    private static void throwValidationExceptionIfErrors(List<String> errors) {
        if (!errors.isEmpty()) {
            throw new ValidationException(String.join("; ", errors));
        }
    }
}

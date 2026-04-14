package com.jb.currencyexchange.validation.structural;

import com.jb.currencyexchange.exception.EmptyFormFieldException;
import com.jb.currencyexchange.exception.validation.CurrencyValidationException;
import com.jb.currencyexchange.exception.ValidationException;
import com.jb.currencyexchange.model.Currency;
import com.jb.currencyexchange.util.CommonValidationUtils;

import java.util.ArrayList;
import java.util.List;

import static com.jb.currencyexchange.util.CommonValidationUtils.*;

public class CurrencyValidation {
    public static void validate(Currency currency) {
        if (currency == null) {
            throw new CurrencyValidationException("Currency object cannot be null");
        }

        List<String> errors = new ArrayList<>();
        validateCurrencyCode(currency.getCode(), "code", errors);
        validateString(
                currency.getName(),
                "name",
                1,
                NAME_MAX_LENGTH,
                null,
                null,
                errors
        );
        validateString(
                currency.getSign(),
                "sign",
                SIGN_MIN_LENGTH,
                SIGN_MAX_LENGTH,
                SIGN_PATTERN,
                "must contain only letters or currency symbols (no spaces or punctuation)",
                errors
        );

        CommonValidationUtils.throwValidationExceptionIfErrors(errors);
    }

    public static void validate(String name, String code, String sign) {
        if (isBlank(name)) {
            throw new EmptyFormFieldException("Name cannot be null or empty");
        } else if (name.length() < 2) {
            throw new ValidationException("Name must be at least 2 characters long");
        } else if (name.length() > 100) {
            throw new ValidationException("Name must not exceed 100 characters");
        } else if (!name.matches("^[a-zA-Z0-9\\s\\p{Sc}]{2,100}$")) {
            throw new ValidationException("Name contains invalid characters. Only Latin letters, digits, spaces and currency symbols allowed");
        }

        if (isBlank(code)) {
            throw new EmptyFormFieldException("Code cannot be null or empty");
        } else if (!CODE_PATTERN.matcher(code).matches()) {
            throw new ValidationException("Code must be 3 uppercase Latin letters");
        }

        if (isBlank(sign)) {
            throw new EmptyFormFieldException("Sign cannot be null or empty");
        } else if (sign.length() > SIGN_MAX_LENGTH) {
            throw new ValidationException(String.format("Sign must not exceed %d characters", SIGN_MAX_LENGTH));
        } else if (!SIGN_PATTERN.matcher(sign).matches()) {
            throw new ValidationException("Sign must contain only letters or currency symbols (no spaces or punctuation)");
        }
    }

    public static void validateCurrencyCodes(String baseCode, String targetCode) {
        validateSingleCurrencyCode(baseCode, "Base currency code");
        validateSingleCurrencyCode(targetCode, "Target currency code");
    }

    private static void validateSingleCurrencyCode(String code, String codeType) {
        if (code == null) {
            throw new IllegalArgumentException(codeType + " cannot be null");
        }
        if (code.trim().isEmpty()) {
            throw new IllegalArgumentException(codeType + " cannot be empty or blank");
        }
        if (code.length() != 3) {
            throw new CurrencyValidationException(codeType + " must be exactly 3 characters long");
        }
        if (!code.matches("[A-Z]{3}")) {
            throw new CurrencyValidationException(codeType + " must contain only uppercase Latin letters (A-Z)");
        }
    }

    public static void validateCurrencyCode(String code, String fieldName, List<String> errors) {
        validateString(code, fieldName, 1, 3, CODE_PATTERN, "must be 3 uppercase Latin letters", errors);
    }
}

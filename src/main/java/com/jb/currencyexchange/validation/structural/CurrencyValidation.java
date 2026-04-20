package com.jb.currencyexchange.validation.structural;

import com.jb.currencyexchange.exception.ValidationException;
import com.jb.currencyexchange.util.ValidationUtils;

public class CurrencyValidation {
    public static void validateCurrency(String name, String code, String sign) {
        if (ValidationUtils.isBlank(name)) {
            throw new ValidationException("Name cannot be null or empty");
        } else if (name.length() < ValidationUtils.NAME_MIN_LENGTH) {
            throw new ValidationException(String.format("Name must be at least %d characters long", ValidationUtils.NAME_MIN_LENGTH));
        } else if (name.length() > ValidationUtils.NAME_MAX_LENGTH) {
            throw new ValidationException(String.format("Name must not exceed %d characters", ValidationUtils.NAME_MAX_LENGTH));
        } else if (!name.matches("^[a-zA-Z0-9\\s\\p{Sc}]{2,100}$")) {
            throw new ValidationException("Name contains invalid characters. Only Latin letters, digits, spaces and currency symbols allowed");
        }

        if (ValidationUtils.isBlank(code)) {
            throw new ValidationException("Code cannot be null or empty");
        } else if (!ValidationUtils.CODE_PATTERN.matcher(code).matches()) {
            throw new ValidationException("Code must be 3 uppercase Latin letters");
        }

        if (ValidationUtils.isBlank(sign)) {
            throw new ValidationException("Sign cannot be null or empty");
        } else if (sign.length() > ValidationUtils.SIGN_MAX_LENGTH) {
            throw new ValidationException(String.format("Sign must not exceed %d characters", ValidationUtils.SIGN_MAX_LENGTH));
        } else if (!ValidationUtils.SIGN_PATTERN.matcher(sign).matches()) {
            throw new ValidationException("Sign must contain only letters or currency symbols (no spaces or punctuation)");
        }
    }

    public static void validateCurrencyCode(String code) {
        if (code == null) {
            throw new ValidationException("Code cannot be null");
        }
        if (code.trim().isEmpty()) {
            throw new ValidationException("Code cannot be empty or blank");
        }
        if (code.length() != 3) {
            throw new ValidationException("Code must be exactly 3 characters long");
        }
        if (!code.matches("[A-Z]{3}")) {
            throw new ValidationException("Code must contain only uppercase Latin letters (A-Z)");
        }
    }
}

package com.jb.currencyexchange.util;

import com.jb.currencyexchange.exception.BadRequestException;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

public final class CommonValidationUtils {
    public static final int NAME_MAX_LENGTH = 50;
    public static final int SIGN_MIN_LENGTH = 1;
    public static final int SIGN_MAX_LENGTH = 3;
    public static final int RATE_MAX_INTEGER_DIGITS = 10;
    public static final int RATE_MAX_FRACTION_DIGITS = 6;
    public static final int AMOUNT_MAX_SCALE = 6;
    public static final int AMOUNT_MAX_DIGITS = 20;
    public static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z]{3}$");
    public static final Pattern SIGN_PATTERN = Pattern.compile("^[a-zA-Z\\p{Sc}]{1,3}$");

    private CommonValidationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void validateString(
            String value,
            String fieldName,
            int minLength,
            int maxLength,
            Pattern pattern,
            String patternMsg,
            List<String> errors
    ) {
        if (isBlank(value)) {
            errors.add(fieldName + " cannot be null or empty");
            return;
        }
        if (value.length() < minLength || value.length() > maxLength) {
            errors.add(fieldName + " must be from " + minLength + " to " + maxLength + " characters");
        }
        if (pattern != null && !pattern.matcher(value).matches()) {
            errors.add(fieldName + " " + patternMsg);
        }
    }

    public static void validateNumber(
            BigDecimal value,
            String fieldName,
            int maxIntegerDigits,
            int maxFractionDigits,
            List<String> errors
    ) {
        if (value == null) {
            errors.add(fieldName + " must be non-null");
            return;
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(fieldName + " must be positive");
            return;
        }

        int scale = value.stripTrailingZeros().scale();
        int integerDigits = value.precision() - Math.max(scale, 0);

        if (integerDigits > maxIntegerDigits) {
            errors.add(fieldName + " must have at most " + maxIntegerDigits + " integer digits");
        }
        if (scale > maxFractionDigits) {
            errors.add(fieldName + " must have at most " + maxFractionDigits + " fractional digits");
        }
    }

    public static void validateExchangeRequestParams(String fromCode, String toCode, BigDecimal amount) {
        validateCurrencyCode(fromCode, "from");
        validateCurrencyCode(toCode, "to");

        if (amount == null) {
            throw new BadRequestException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Amount must be non-negative");
        }
    }

    private static void validateCurrencyCode(String code, String paramName) {
        if (code == null || code.trim().isEmpty()) {
            throw new BadRequestException("Currency '" + paramName + "' code cannot be null or empty");
        }
    }

    public static void validateFields(
            Map<String, String> fields,
            BiConsumer<String, String> validator
    ) {
        fields.forEach((fieldName, value) -> validator.accept(value, fieldName));
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static void throwValidationExceptionIfErrors(List<String> errors) {
        if (!errors.isEmpty()) {
            throw new IllegalArgumentException("Validation failed: " + String.join("; ", errors));
        }
    }

    public static boolean isUniqueConstraintViolation(SQLException e) {
        String message = e.getMessage().toLowerCase();

        return message.contains("unique constraint failed") ||
                message.contains("column code is not unique");
    }
}

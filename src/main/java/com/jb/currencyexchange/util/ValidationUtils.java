package com.jb.currencyexchange.util;

import java.math.BigDecimal;
import java.util.regex.Pattern;

public final class ValidationUtils {
    public static final int NAME_MIN_LENGTH = 2;
    public static final int NAME_MAX_LENGTH = 50;
    public static final int SIGN_MAX_LENGTH = 3;
    public static final int RATE_MAX_INTEGER_DIGITS = 10;
    public static final int RATE_MAX_FRACTION_DIGITS = 6;
    public static final int CURRENCY_PAIR_LENGTH = 6;
    public static final int AMOUNT_MAX_SCALE = 6;
    public static final int AMOUNT_MAX_DIGITS = 20;
    public static final Pattern CODE_PATTERN = Pattern.compile("^[A-Z]{3}$");
    public static final Pattern SIGN_PATTERN = Pattern.compile("^[a-zA-Z\\p{Sc}]{1,3}$");

    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void validateNumber(
            BigDecimal value,
            String fieldName,
            int maxDigits,
            int maxFractionDigits,
            boolean validateTotalDigits
    ) {
        if (value == null) {
            throw new NumberFormatException(fieldName + " must be non-null");
        }
        if (value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new NumberFormatException(fieldName + " must be positive");
        }

        BigDecimal normalized = value.stripTrailingZeros();
        int scale = normalized.scale();
        int totalDigits = normalized.precision();
        int integerDigits = normalized.precision() - Math.max(scale, 0);

        if (validateTotalDigits) {
            if (totalDigits > maxDigits) {
                throw new NumberFormatException(fieldName + " must contain at most " + maxDigits + " digits");
            }
        } else if (integerDigits > maxDigits) {
            throw new NumberFormatException(fieldName + " must have at most " + maxDigits + " integer digits");
        }

        if (scale > maxFractionDigits) {
            throw new NumberFormatException(fieldName + " must have at most " + maxFractionDigits + " fractional digits");
        }
    }

    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }
}

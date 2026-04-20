package com.jb.currencyexchange.validation.business;

import com.jb.currencyexchange.exception.ValidationException;

import java.util.Set;

public final class InputSecurityValidation {
    private static final String SECURITY_REJECTION_MESSAGE = "Name contains disallowed content";
    private static final Set<String> FORBIDDEN_WORDS = Set.of(
            "gaysex", "fuck", "shit", "bitch", "asshole", "cunt", "whore",
            "porn", "anal", "rape", "sex", "nigger", "nigga", "fag"
    );

    private InputSecurityValidation() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void validateCurrencyNameWrite(String name) {
        validateNoForbiddenContent(name);
    }

    private static void validateNoForbiddenContent(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        for (String token : value.toLowerCase().split("[^a-z0-9]+")) {
            if (!token.isEmpty() && FORBIDDEN_WORDS.contains(token)) {
                throw new ValidationException(SECURITY_REJECTION_MESSAGE);
            }
        }
    }
}

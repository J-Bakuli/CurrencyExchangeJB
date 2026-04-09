package com.jb.currencyexchange.validation.business;

import com.jb.currencyexchange.exception.validation.ValidationException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

public final class InputSecurityValidation {
    private static final String SECURITY_REJECTION_MESSAGE = "Input rejected by security policy";
    private static final String FORBIDDEN_WORDS_RESOURCE = "forbidden-words.txt";

    private static final Set<String> FORBIDDEN_WORDS_FALLBACK = Set.of(
            "gaysex", "fuck", "shit", "bitch", "asshole", "cunt", "whore",
            "porn", "anal", "rape", "sex", "nigger", "nigga", "fag"
    );

    private static final Set<String> FORBIDDEN_WORDS = loadSetFromResource(FORBIDDEN_WORDS_RESOURCE, FORBIDDEN_WORDS_FALLBACK);

    private InputSecurityValidation() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static void validateCurrencyWrite(String name, String code, String sign) {
        validateNoForbiddenContent(name);
        validateNoForbiddenContent(code);
        validateNoForbiddenContent(sign);
    }

    public static void validateExchangeRateWrite(String baseCode, String targetCode) {
        validateNoForbiddenContent(baseCode);
        validateNoForbiddenContent(targetCode);
    }

    private static void validateNoForbiddenContent(String value) {
        if (value == null || value.isBlank()) {
            return;
        }
        String normalized = normalizeForModeration(value);
        if (normalized.isBlank()) {
            return;
        }
        boolean hasForbidden = FORBIDDEN_WORDS.stream().anyMatch(normalized::contains);
        if (hasForbidden) {
            throw new ValidationException(SECURITY_REJECTION_MESSAGE);
        }
    }

    private static String normalizeForModeration(String value) {
        String lower = value.toLowerCase();
        StringBuilder sb = new StringBuilder(lower.length());
        for (int i = 0; i < lower.length(); i++) {
            char ch = lower.charAt(i);
            switch (ch) {
                case '0' -> sb.append('o');
                case '1', '!' -> sb.append('i');
                case '3' -> sb.append('e');
                case '4', '@' -> sb.append('a');
                case '5', '$' -> sb.append('s');
                case '7' -> sb.append('t');
                case '8' -> sb.append('b');
                default -> {
                    if (Character.isLetterOrDigit(ch)) {
                        sb.append(ch);
                    }
                }
            }
        }
        return sb.toString();
    }

    private static Set<String> loadSetFromResource(String resource, Set<String> fallback) {
        InputStream input = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
        if (input == null) {
            return fallback;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            Set<String> loaded = reader.lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .filter(line -> !line.startsWith("#"))
                    .map(String::toLowerCase)
                    .collect(Collectors.toSet());
            if (loaded.isEmpty()) {
                return fallback;
            }
            return Collections.unmodifiableSet(loaded);
        } catch (Exception e) {
            return fallback;
        }
    }
}

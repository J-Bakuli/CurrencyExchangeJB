package com.jb.currencyexchange.util;

import com.jb.currencyexchange.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class StringUtils {
    private StringUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String cleanString(String input, String param) {
        if (input == null) {
            throw new ValidationException(param + " is null");
        }
        String cleanInput = input.trim();
        if (cleanInput.isEmpty()) {
            throw new ValidationException(param + " is empty");
        }
        return cleanInput;
    }
}

package com.jb.currencyexchange.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class StringUtils {
    private StringUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String cleanString(String input) {
        if (input == null) {
            return "";
        }
        return input.trim();
    }
}

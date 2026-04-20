package com.jb.currencyexchange.util;

import com.jb.currencyexchange.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class PathUtils {
    private PathUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static String extractCurrencyCode(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.trim().equals("/")) {
            throw new ValidationException("Currency code path is required, e.g. /USD");
        }

        String[] segments = pathInfo.split("/");
        if (segments.length != 2 || segments[1].isEmpty()) {
            throw new ValidationException("Invalid path format. Expected /{CODE}, e.g. /USD");
        }

        String code = segments[1].trim();
        if (code.length() != (CommonValidationUtils.CURRENCY_PAIR_LENGTH)/2) {
            throw new ValidationException("Currency code must be exactly 3 characters long");
        }
        return code;
    }

    public static String[] extractCurrencyPair(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        log.debug("Raw pathInfo: {}", pathInfo);

        if (pathInfo == null || pathInfo.isEmpty()) {
            throw new ValidationException("Path info is missing");
        }

        pathInfo = pathInfo.replaceAll("^/+", "");
        log.debug("Cleaned pathInfo: {}", pathInfo);

        if (pathInfo.length() != CommonValidationUtils.CURRENCY_PAIR_LENGTH) {
            log.warn("Currency pair length is not {}: {}", CommonValidationUtils.CURRENCY_PAIR_LENGTH, pathInfo);
            throw new ValidationException(
                    String.format("Currency pair must be %d characters long", CommonValidationUtils.CURRENCY_PAIR_LENGTH)
            );
        }

        String baseCode = pathInfo.substring(0, (CommonValidationUtils.CURRENCY_PAIR_LENGTH)/2);
        String targetCode = pathInfo.substring((CommonValidationUtils.CURRENCY_PAIR_LENGTH)/2, CommonValidationUtils.CURRENCY_PAIR_LENGTH);
        log.debug("Parsed currencies: base={}, target={}", baseCode, targetCode);

        return new String[]{baseCode, targetCode};
    }
}
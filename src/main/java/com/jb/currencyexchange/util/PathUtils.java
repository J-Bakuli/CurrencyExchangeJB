package com.jb.currencyexchange.util;

import com.jb.currencyexchange.exception.BadRequestException;
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
            return null;
        }

        String[] segments = pathInfo.split("/");
        if (segments.length != 2 || segments[1].isEmpty()) {
            return null;
        }

        String code = segments[1].trim();
        if (code.length() != 3) {
            return null;
        }
        return code;
    }

    public static String[] extractCurrencyPair(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        log.debug("Raw pathInfo: {}", pathInfo);

        if (pathInfo == null || pathInfo.isEmpty()) {
            throw new BadRequestException("Path info is missing");
        }

        pathInfo = pathInfo.replaceAll("^/+", "");
        log.debug("Cleaned pathInfo: {}", pathInfo);

        if (pathInfo.length() != 6) {
            log.warn("Currency pair length is not 6: {}", pathInfo);
            throw new BadRequestException("Currency pair must be 6 characters long");
        }

        String baseCode = pathInfo.substring(0, 3);
        String targetCode = pathInfo.substring(3, 6);
        log.debug("Parsed currencies: base={}, target={}", baseCode, targetCode);

        return new String[]{baseCode, targetCode};
    }
}
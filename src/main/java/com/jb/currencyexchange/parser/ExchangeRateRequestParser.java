package com.jb.currencyexchange.parser;

import com.jb.currencyexchange.dto.request.CreateExchangeRateRequestDto;
import com.jb.currencyexchange.dto.request.UpdateExchangeRateRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public final class ExchangeRateRequestParser {
    private ExchangeRateRequestParser() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static ParseResult<CreateExchangeRateRequestDto> parseCreateRequest(HttpServletRequest req) {
        String baseCode = req.getParameter("baseCurrencyCode");
        String targetCode = req.getParameter("targetCurrencyCode");
        String rateParam = req.getParameter("rate");

        if (baseCode == null || baseCode.trim().isEmpty()) {
            return ParseResult.error("Base currency code is required.");
        }
        if (targetCode == null || targetCode.trim().isEmpty()) {
            return ParseResult.error("Target currency code is required.");
        }
        if (rateParam == null || rateParam.trim().isEmpty()) {
            return ParseResult.error("Rate is required. It must be a valid decimal number.");
        }
        try {
            BigDecimal rate = new BigDecimal(rateParam.trim());
            return ParseResult.success(new CreateExchangeRateRequestDto(baseCode, targetCode, rate));
        } catch (NumberFormatException e) {
            return ParseResult.error("Rate must be a valid decimal number.");
        }
    }

    public static ParseResult<UpdateExchangeRateRequestDto> parseUpdateRequest(HttpServletRequest req) {
        String pathInfo = req.getPathInfo();
        if (pathInfo == null || pathInfo.trim().isEmpty()) {
            return ParseResult.error("Currency pair is required");
        }

        String currencyPair = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
        if (currencyPair.length() != 6) {
            return ParseResult.error("Currency pair must be 6 characters long");
        }

        String baseCode = currencyPair.substring(0, 3);
        String targetCode = currencyPair.substring(3, 6);

        String rateParam = req.getParameter("rate");
        if (rateParam == null) {
            rateParam = readFormUrlEncodedRateFromBody(req);
        }
        if (rateParam == null || rateParam.trim().isEmpty()) {
            return ParseResult.error("Rate is required. It must be a valid decimal number.");
        }

        try {
            BigDecimal rate = new BigDecimal(rateParam.trim());
            return ParseResult.success(new UpdateExchangeRateRequestDto(baseCode, targetCode, rate));
        } catch (NumberFormatException e) {
            return ParseResult.error("Rate must be a valid decimal number.");
        }
    }

    private static String readFormUrlEncodedRateFromBody(HttpServletRequest req) {
        String contentType = req.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains("application/x-www-form-urlencoded")) {
            return null;
        }
        String body;
        try {
            body = req.getReader().lines().reduce("", String::concat);
        } catch (IOException e) {
            log.debug("Failed to read request body for rate parameter", e);
            return null;
        }
        if (body == null || body.isBlank()) {
            return null;
        }
        Map<String, String> params = parseFormUrlEncoded(body);
        return params.get("rate");
    }

    private static Map<String, String> parseFormUrlEncoded(String body) {
        Map<String, String> result = new HashMap<>();
        for (String pair : body.split("&")) {
            if (pair.isEmpty()) {
                continue;
            }
            int idx = pair.indexOf('=');
            String rawKey = idx >= 0 ? pair.substring(0, idx) : pair;
            String rawVal = idx >= 0 ? pair.substring(idx + 1) : "";
            String key = URLDecoder.decode(rawKey, StandardCharsets.UTF_8);
            String value = URLDecoder.decode(rawVal, StandardCharsets.UTF_8);
            result.put(key, value);
        }
        return result;
    }
}

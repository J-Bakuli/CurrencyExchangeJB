package com.jb.currencyexchange.parser;

import com.jb.currencyexchange.dto.request.CreateExchangeRateRequestDto;
import com.jb.currencyexchange.dto.request.UpdateExchangeRateRequestDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ExchangeRateRequestParser {
    public static ParseResult<CreateExchangeRateRequestDto> parseCreateRequest(HttpServletRequest req) {
        String rateParam = null;
        try {
            String baseCode = req.getParameter("baseCurrencyCode");
            String targetCode = req.getParameter("targetCurrencyCode");
            rateParam = req.getParameter("rate");

            List<String> missingFields = new ArrayList<>();

            if (baseCode == null || baseCode.trim().isEmpty()) {
                missingFields.add("baseCurrencyCode");
            }
            if (targetCode == null || targetCode.trim().isEmpty()) {
                missingFields.add("targetCurrencyCode");
            }
            if (rateParam == null || rateParam.trim().isEmpty()) {
                missingFields.add("rate");
            }

            if (!missingFields.isEmpty()) {
                return ParseResult.error("Invalid request data",
                        missingFields);
            }

            BigDecimal rate = new BigDecimal(rateParam);
            return ParseResult.success(new CreateExchangeRateRequestDto(baseCode, targetCode, rate));
        } catch (NumberFormatException e) {
            return ParseResult.error("Invalid request data",
                    List.of("rate"));
        }
    }

    public static ParseResult<UpdateExchangeRateRequestDto> parseUpdateRequest(HttpServletRequest req) {
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.length() < 4) {
                log.debug("Invalid path: {}", pathInfo);
                return ParseResult.error(
                        "Invalid request data",
                        List.of("currencyPair")
                );
            }

            String currencyPair = pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;

            if (currencyPair.length() != 6) {
                log.debug("Currency pair must be exactly 6 characters: {}", currencyPair);
                return ParseResult.error(
                        "Invalid request data",
                        List.of("currencyPair")
                );
            }

            String baseCode = currencyPair.substring(0, 3);
            String targetCode = currencyPair.substring(3, 6);

            String rateParam = req.getParameter("rate");
            if (rateParam == null) {
                rateParam = readFormUrlEncodedParamFromBody(req);
            }
            if (rateParam == null || rateParam.trim().isEmpty()) {
                log.debug("Missing required parameter: rate");
                return ParseResult.error("Invalid request data", List.of("rate"));
            }

            BigDecimal rate;
            try {
                rate = new BigDecimal(rateParam.trim());
                if (rate.compareTo(BigDecimal.ZERO) <= 0) {
                    log.debug("Invalid rate value: must be greater than zero");
                    return ParseResult.error("Invalid request data", List.of("rate"));
                }
            } catch (NumberFormatException e) {
                log.debug("Failed to parse rate as BigDecimal: {}", rateParam, e);
                return ParseResult.error(
                        "Invalid request data",
                        List.of("rate")
                );
            }

            UpdateExchangeRateRequestDto dto = new UpdateExchangeRateRequestDto(baseCode, targetCode, rate);
            return ParseResult.success(dto);
        } catch (Exception e) {
            log.error("Unexpected error parsing update request", e);
            return ParseResult.error(
                    "Invalid request data",
                    List.of("request")
            );
        }
    }

    private static String readFormUrlEncodedParamFromBody(HttpServletRequest req) {
        String contentType = req.getContentType();
        if (contentType == null || !contentType.toLowerCase().contains("application/x-www-form-urlencoded")) {
            return null;
        }

        String body;
        try {
            body = req.getReader().lines().reduce("", (a, b) -> a.isEmpty() ? b : a + "\n" + b);
        } catch (IOException e) {
            log.debug("Failed to read request body for form parsing", e);
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
            String key = urlDecode(rawKey);
            String val = urlDecode(rawVal);
            result.put(key, val);
        }
        return result;
    }

    private static String urlDecode(String s) {
        return URLDecoder.decode(s, StandardCharsets.UTF_8);
    }
}
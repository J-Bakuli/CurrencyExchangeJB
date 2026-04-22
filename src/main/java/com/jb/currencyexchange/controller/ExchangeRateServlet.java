package com.jb.currencyexchange.controller;

import com.jb.currencyexchange.db.AppLifecycleListener;
import com.jb.currencyexchange.dto.request.UpdateExchangeRateRequestDto;
import com.jb.currencyexchange.dto.response.ExchangeRateResponseDto;
import com.jb.currencyexchange.exception.ValidationException;
import com.jb.currencyexchange.service.ExchangeRateService;
import com.jb.currencyexchange.util.PathUtils;
import com.jb.currencyexchange.validation.structural.CurrencyValidation;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@WebServlet("/exchangeRate/*")
@Slf4j
public class ExchangeRateServlet extends BaseServlet {
    private ExchangeRateService exchangeRateService;

    @Override
    public void init() throws ServletException {
        exchangeRateService = (ExchangeRateService) getServletContext().getAttribute(AppLifecycleListener.EXCHANGE_RATE_SERVICE_ATTR);
        if (exchangeRateService == null) {
            throw new ServletException("ExchangeRateService is not initialized");
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String baseCode;
        String targetCode;
        try {
            String pathInfo = req.getPathInfo();
            if (pathInfo == null || pathInfo.trim().isEmpty()) {
                throw new ValidationException("Currency pair is required");
            }
            String normalizedPath = pathInfo.replaceAll("^/+|/+$", "");
            req.setAttribute("normalizedPath", normalizedPath);
            String[] pair = PathUtils.extractCurrencyPair(req);
            baseCode = pair[0];
            targetCode = pair[1];
            log.info("GET /exchangeRate/{} - fetching rate for {}/{}", normalizedPath, baseCode, targetCode);
            CurrencyValidation.validateCurrencyCode(baseCode);
            CurrencyValidation.validateCurrencyCode(targetCode);
            ExchangeRateResponseDto exchangeRate = exchangeRateService.getRate(baseCode, targetCode);
            sendSuccessResponse(resp, exchangeRate);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    protected void doPatch(HttpServletRequest req, HttpServletResponse resp) {
        try {
            log.info("Handling PATCH /exchangeRate request for path: {}", req.getPathInfo());
            UpdateExchangeRateRequestDto dto = parseUpdateRequest(req);
            ExchangeRateResponseDto updatedRate = exchangeRateService.update(dto);
            sendSuccessResponse(resp, updatedRate);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void destroy() {
        log.info("Destroying Servlet 'ExchangeRateServlet' — application is shutting down");
    }

    private UpdateExchangeRateRequestDto parseUpdateRequest(HttpServletRequest req) {
        String[] pair = PathUtils.extractCurrencyPair(req);
        BigDecimal rate = parseRate(requiredRateParameter(req));
        return new UpdateExchangeRateRequestDto(pair[0], pair[1], rate);
    }

    private String requiredRateParameter(HttpServletRequest req) {
        String rateParam = extractRateParameter(req);
        if (rateParam == null || rateParam.trim().isEmpty()) {
            throw new ValidationException("Rate is required. It must be a valid decimal number.");
        }
        return rateParam;
    }

    private String extractRateParameter(HttpServletRequest req) {
        String rateParam = req.getParameter("rate");
        if (rateParam != null) {
            return rateParam;
        }

        String body;
        try {
            body = req.getReader().lines().reduce("", String::concat);
        } catch (IOException e) {
            log.debug("Failed to read request body for rate parameter", e);
            return null;
        }

        for (String formPart : body.split("&")) {
            if (formPart.isBlank()) {
                continue;
            }
            String[] keyValue = formPart.split("=", 2);
            String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
            if (!"rate".equals(key)) {
                continue;
            }
            return keyValue.length > 1
                    ? URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8)
                    : "";
        }
        return null;
    }

    private BigDecimal parseRate(String rawRate) {
        try {
            return new BigDecimal(rawRate.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Rate must be a valid decimal number.");
        }
    }
}

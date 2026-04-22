package com.jb.currencyexchange.controller;

import com.jb.currencyexchange.db.AppLifecycleListener;
import com.jb.currencyexchange.dto.request.CreateExchangeRateRequestDto;
import com.jb.currencyexchange.dto.response.ExchangeRateResponseDto;
import com.jb.currencyexchange.exception.ValidationException;
import com.jb.currencyexchange.service.ExchangeRateService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@WebServlet("/exchangeRates")
@Slf4j
public class ExchangeRatesServlet extends BaseServlet {
    private ExchangeRateService exchangeRateService;

    @Override
    public void init() throws ServletException {
        exchangeRateService = (ExchangeRateService) getServletContext().getAttribute(AppLifecycleListener.EXCHANGE_RATE_SERVICE_ATTR);
        if (exchangeRateService == null) {
            throw new ServletException("ExchangeRateService is not initialized");
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<ExchangeRateResponseDto> rates = exchangeRateService.getAllRates();
        sendJsonResponse(resp, rates, HttpServletResponse.SC_OK);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("Handling POST /exchangeRates request");
        CreateExchangeRateRequestDto dto = parseCreateRequest(req);
        ExchangeRateResponseDto result = exchangeRateService.create(dto);
        sendCreationSuccessResponse(resp, result);
    }

    @Override
    public void destroy() {
        log.info("Destroying Servlet 'ExchangeRatesServlet' — application is shutting down");
    }

    private CreateExchangeRateRequestDto parseCreateRequest(HttpServletRequest req) {
        String baseCode = requiredParam(req, "baseCurrencyCode", "Base currency code is required.");
        String targetCode = requiredParam(req, "targetCurrencyCode", "Target currency code is required.");
        BigDecimal rate = parseRate(requiredParam(req, "rate", "Rate is required. It must be a valid decimal number."));
        return new CreateExchangeRateRequestDto(baseCode, targetCode, rate);
    }

    private String requiredParam(HttpServletRequest req, String parameterName, String errorMessage) {
        String value = req.getParameter(parameterName);
        if (value == null || value.trim().isEmpty()) {
            throw new ValidationException(errorMessage);
        }
        return value;
    }

    private BigDecimal parseRate(String rawRate) {
        try {
            return new BigDecimal(rawRate.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Rate must be a valid decimal number.");
        }
    }
}

package com.jb.currencyexchange.controller;

import com.jb.currencyexchange.dao.CurrencyDao;
import com.jb.currencyexchange.dao.ExchangeRateDao;
import com.jb.currencyexchange.dao.JdbcCurrencyDao;
import com.jb.currencyexchange.dao.JdbcExchangeRateDao;
import com.jb.currencyexchange.dto.request.UpdateExchangeRateRequestDto;
import com.jb.currencyexchange.dto.response.ExchangeRateResponseDto;
import com.jb.currencyexchange.exception.ValidationException;
import com.jb.currencyexchange.mapper.ExchangeRateMapper;
import com.jb.currencyexchange.service.ExchangeRateService;
import com.jb.currencyexchange.util.PathUtils;
import com.jb.currencyexchange.validation.business.InputSecurityValidation;
import com.jb.currencyexchange.validation.structural.CurrencyValidation;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@WebServlet("/exchangeRate/*")
@Slf4j
public class ExchangeRateServlet extends BaseServlet {
    private ExchangeRateService exchangeRateService;

    @Override
    public void init() throws ServletException {
        try {
            ExchangeRateDao exchangeRateDao = new JdbcExchangeRateDao();
            CurrencyDao currencyDao = new JdbcCurrencyDao();
            ExchangeRateMapper mapper = ExchangeRateMapper.INSTANCE;
            this.exchangeRateService = new ExchangeRateService(
                    exchangeRateDao,
                    currencyDao,
                    mapper
            );
            log.info("ExchangeRateService is initialized successfully");
        } catch (Exception e) {
            log.error("ExchangeRateService initialization error", e);
            throw new ServletException("Failed to initialize service", e);
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
            CurrencyValidation.validateCurrencyCodes(baseCode, targetCode);
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
            InputSecurityValidation.validateExchangeRateWrite(dto.baseCode(), dto.targetCode());
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
        String baseCode;
        String targetCode;
        BigDecimal rate;
        try {
            String[] pair = PathUtils.extractCurrencyPair(req);
            baseCode = pair[0];
            targetCode = pair[1];
            String rateStrParam = req.getParameter("rate");
            if (rateStrParam == null || rateStrParam.trim().isEmpty()) {
                log.warn("Rate is missing or blank.");
                throw new ValidationException("Rate is required. It must be a valid decimal number.");
            }
            rate = new BigDecimal(rateStrParam.trim());
        } catch (NumberFormatException e) {
            log.warn("Rate must be a valid decimal number.");
            throw new ValidationException("Rate must be a valid decimal number.");
        }

        return new UpdateExchangeRateRequestDto(baseCode, targetCode, rate);
    }
}

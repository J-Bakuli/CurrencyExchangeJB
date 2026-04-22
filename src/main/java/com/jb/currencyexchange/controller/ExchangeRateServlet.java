package com.jb.currencyexchange.controller;

import com.jb.currencyexchange.db.AppLifecycleListener;
import com.jb.currencyexchange.dto.request.UpdateExchangeRateRequestDto;
import com.jb.currencyexchange.dto.response.ExchangeRateResponseDto;
import com.jb.currencyexchange.exception.ValidationException;
import com.jb.currencyexchange.parser.ExchangeRateRequestParser;
import com.jb.currencyexchange.parser.ParseResult;
import com.jb.currencyexchange.service.ExchangeRateService;
import com.jb.currencyexchange.util.PathUtils;
import com.jb.currencyexchange.validation.structural.CurrencyValidation;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

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
            ParseResult<UpdateExchangeRateRequestDto> parseResult = ExchangeRateRequestParser.parseUpdateRequest(req);
            if (!parseResult.isSuccess()) {
                throw new ValidationException(parseResult.getErrorMessage());
            }
            UpdateExchangeRateRequestDto dto = parseResult.getData();
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
}

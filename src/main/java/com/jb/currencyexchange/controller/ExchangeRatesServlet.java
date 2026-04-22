package com.jb.currencyexchange.controller;

import com.jb.currencyexchange.db.AppLifecycleListener;
import com.jb.currencyexchange.dto.request.CreateExchangeRateRequestDto;
import com.jb.currencyexchange.dto.response.ExchangeRateResponseDto;
import com.jb.currencyexchange.exception.ValidationException;
import com.jb.currencyexchange.parser.ExchangeRateRequestParser;
import com.jb.currencyexchange.parser.ParseResult;
import com.jb.currencyexchange.service.ExchangeRateService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

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
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            List<ExchangeRateResponseDto> rates = exchangeRateService.getAllRates();
            sendJsonResponse(resp, rates, HttpServletResponse.SC_OK);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            log.info("Handling POST /exchangeRates request");
            ParseResult<CreateExchangeRateRequestDto> parseResult = ExchangeRateRequestParser.parseCreateRequest(req);
            if (!parseResult.isSuccess()) {
                log.warn("Failed to parse request: {}", parseResult.getErrorMessage());
                throw new ValidationException(parseResult.getErrorMessage());
            }
            CreateExchangeRateRequestDto dto = parseResult.getData();
            ExchangeRateResponseDto result = exchangeRateService.create(dto);
            sendCreationSuccessResponse(resp, result);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void destroy() {
        log.info("Destroying Servlet 'ExchangeRatesServlet' — application is shutting down");
    }
}

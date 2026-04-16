package com.jb.currencyexchange.controller;

import com.jb.currencyexchange.dao.JdbcCurrencyDao;
import com.jb.currencyexchange.dao.JdbcExchangeRateDao;
import com.jb.currencyexchange.dto.ExchangeResultDto;
import com.jb.currencyexchange.service.ExchangeRateCalculatorService;
import com.jb.currencyexchange.util.StringUtils;
import com.jb.currencyexchange.validation.structural.ExchangeRateValidation;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;

@WebServlet("/exchange")
@Slf4j
public class ExchangeServlet extends BaseServlet {
    private ExchangeRateCalculatorService exchangeRateCalculatorService;

    @Override
    public void init() throws ServletException {
        try {
            JdbcCurrencyDao currencyDao = new JdbcCurrencyDao();
            JdbcExchangeRateDao exchangeRateDao = new JdbcExchangeRateDao();
            this.exchangeRateCalculatorService = new ExchangeRateCalculatorService(currencyDao, exchangeRateDao);
            log.info("ExchangeService is initialized successfully");
        } catch (Exception e) {
            log.error("ExchangeService initialization error", e);
            throw new ServletException("Failed to initialize service", e);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        String from = StringUtils.cleanString(req.getParameter("from"));
        String to = StringUtils.cleanString(req.getParameter("to"));
        String amountStr = StringUtils.cleanString(req.getParameter("amount"));
        log.info("GET /exchange from={}, to={}, amount={}", from, to, amountStr);
        try {
            ExchangeRateValidation.validateExchangeParams(from, to, amountStr);
            BigDecimal amount = new BigDecimal(amountStr.trim());
            ExchangeResultDto resultDto = exchangeRateCalculatorService.calculate(from, to, amount);
            sendSuccessResponse(resp, resultDto);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void destroy() {
        log.info("Destroying Servlet 'ExchangeServlet' — application is shutting down");
    }
}

package com.jb.currencyexchange.controller;

import com.jb.currencyexchange.dao.CurrencyDao;
import com.jb.currencyexchange.dao.ExchangeRateDao;
import com.jb.currencyexchange.dao.JdbcCurrencyDao;
import com.jb.currencyexchange.dao.JdbcExchangeRateDao;
import com.jb.currencyexchange.dto.request.UpdateExchangeRateRequestDto;
import com.jb.currencyexchange.dto.response.ExchangeRateResponseDto;
import com.jb.currencyexchange.exception.ValidationException;
import com.jb.currencyexchange.mapper.ExchangeRateMapper;
import com.jb.currencyexchange.parser.ExchangeRateRequestParser;
import com.jb.currencyexchange.parser.ParseResult;
import com.jb.currencyexchange.service.ExchangeRateService;
import com.jb.currencyexchange.util.PathUtils;
import com.jb.currencyexchange.validation.business.CurrencyBusinessValidation;
import com.jb.currencyexchange.validation.business.InputSecurityValidation;
import com.jb.currencyexchange.validation.structural.CurrencyValidation;
import com.jb.currencyexchange.validation.structural.DtoValidation;
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
        try {
            ExchangeRateDao exchangeRateDao = new JdbcExchangeRateDao();
            CurrencyDao currencyDao = new JdbcCurrencyDao();
            CurrencyBusinessValidation validation = new CurrencyBusinessValidation(currencyDao);
            ExchangeRateMapper mapper = ExchangeRateMapper.INSTANCE;
            this.exchangeRateService = new ExchangeRateService(
                    exchangeRateDao,
                    currencyDao,
                    validation,
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
        String baseCode = null;
        String targetCode = null;
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
            ParseResult<UpdateExchangeRateRequestDto> parseResult = ExchangeRateRequestParser.parseUpdateRequest(req);
            if (!parseResult.isSuccess()) {
                throw new ValidationException(
                        parseResult.getErrorMessage()
                );
            }
            UpdateExchangeRateRequestDto dto = parseResult.getData();
            InputSecurityValidation.validateExchangeRateWrite(dto.baseCode(), dto.targetCode());
            DtoValidation.validate(dto);
            ExchangeRateResponseDto updatedRate = exchangeRateService.update(dto);
            resp.setStatus(HttpServletResponse.SC_OK);
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

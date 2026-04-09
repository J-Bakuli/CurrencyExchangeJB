package com.jb.currencyexchange.controller;

import com.jb.currencyexchange.dao.CurrencyDao;
import com.jb.currencyexchange.dao.ExchangeRateDao;
import com.jb.currencyexchange.dao.JdbcCurrencyDao;
import com.jb.currencyexchange.dao.JdbcExchangeRateDao;
import com.jb.currencyexchange.dto.request.CreateExchangeRateRequestDto;
import com.jb.currencyexchange.dto.response.ExchangeRateResponseDto;
import com.jb.currencyexchange.exception.BadRequestException;
import com.jb.currencyexchange.mapper.ExchangeRateMapper;
import com.jb.currencyexchange.parser.ExchangeRateRequestParser;
import com.jb.currencyexchange.parser.ParseResult;
import com.jb.currencyexchange.service.ExchangeRateService;
import com.jb.currencyexchange.validation.business.CurrencyBusinessValidation;
import com.jb.currencyexchange.validation.business.InputSecurityValidation;
import com.jb.currencyexchange.validation.structural.DtoValidation;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.List;

@WebServlet("/exchangeRates")
@Slf4j
public class ExchangeRatesServlet extends BaseServlet {
    private ExchangeRateService exchangeRateService;

    @Override
    public void init() throws ServletException {
        try {
            ExchangeRateDao exchangeRateDao = new JdbcExchangeRateDao();
            CurrencyDao currencyDao = new JdbcCurrencyDao();
            ExchangeRateMapper mapper = ExchangeRateMapper.INSTANCE;
            CurrencyBusinessValidation currencyBusinessValidation = new CurrencyBusinessValidation(currencyDao);
            this.exchangeRateService = new ExchangeRateService(
                    exchangeRateDao,
                    currencyDao,
                    currencyBusinessValidation,
                    mapper
            );
            log.info("ExchangeRateService is initialized successfully");
        } catch (Exception e) {
            log.error("ExchangeRateService initialization error", e);
            throw new ServletException("Failed to initialize service", e);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
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
                throw new BadRequestException(
                        parseResult.getErrorMessage(),
                        java.util.Map.of("fields", parseResult.getMissingFields())
                );
            }
            CreateExchangeRateRequestDto dto = parseResult.getData();
            InputSecurityValidation.validateExchangeRateWrite(dto.baseCode(), dto.targetCode());
            DtoValidation.validate(dto);
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

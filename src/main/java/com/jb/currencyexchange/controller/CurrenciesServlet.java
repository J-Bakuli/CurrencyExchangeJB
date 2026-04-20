package com.jb.currencyexchange.controller;

import com.jb.currencyexchange.dao.JdbcCurrencyDao;
import com.jb.currencyexchange.dto.request.CreateCurrencyRequestDto;
import com.jb.currencyexchange.dto.response.CurrencyResponseDto;
import com.jb.currencyexchange.mapper.CurrencyMapper;
import com.jb.currencyexchange.service.CurrencyService;
import com.jb.currencyexchange.util.StringUtils;
import com.jb.currencyexchange.validation.business.InputSecurityValidation;
import com.jb.currencyexchange.validation.structural.CurrencyValidation;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

@WebServlet({"/currencies", "/currencies/*"})
@Slf4j
public class CurrenciesServlet extends BaseServlet {
    private CurrencyService currencyService;

    @Override
    public void init() throws ServletException {
        try {
            JdbcCurrencyDao currencyDao = new JdbcCurrencyDao();
            CurrencyMapper mapper = CurrencyMapper.INSTANCE;
            this.currencyService = new CurrencyService(currencyDao, mapper);
            log.info("CurrencyService is initialized successfully");
        } catch (Exception e) {
            log.error("CurrencyService initialization error", e);
            throw new ServletException("Failed to initialize service", e);
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            log.info("GET /currencies getAll()");
            List<CurrencyResponseDto> dtos = currencyService.getAll();
            if (dtos.isEmpty()) {
                sendSuccessResponse(resp, Collections.emptyList());
                return;
            }
            sendSuccessResponse(resp, dtos);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        String name = StringUtils.cleanString(req.getParameter("name"), "name");
        String code = StringUtils.cleanString(req.getParameter("code"), "code");
        String sign = StringUtils.cleanString(req.getParameter("sign"), "sign");
        log.info("POST /currencies - received: name='{}', code='{}', sign='{}'",
                name != null ? name : "null",
                code != null ? code : "null",
                sign != null ? sign : "null");
        try {
            InputSecurityValidation.validateCurrencyNameWrite(name);
            CurrencyValidation.validateCurrency(name, code, sign);
            CreateCurrencyRequestDto requestDto = new CreateCurrencyRequestDto(name, code, sign);
            CurrencyResponseDto createdDto = currencyService.create(requestDto);
            log.info("Currency created successfully: code='{}'", code);
            sendCreationSuccessResponse(resp, createdDto);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void destroy() {
        log.info("Destroying Servlet 'CurrenciesServlet' — application is shutting down");
    }
}

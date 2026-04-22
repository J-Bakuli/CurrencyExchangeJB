package com.jb.currencyexchange.controller;

import com.jb.currencyexchange.db.AppLifecycleListener;
import com.jb.currencyexchange.dto.request.CreateCurrencyRequestDto;
import com.jb.currencyexchange.dto.response.CurrencyResponseDto;
import com.jb.currencyexchange.service.CurrencyService;
import com.jb.currencyexchange.validation.business.InputSecurityValidation;
import com.jb.currencyexchange.validation.structural.CurrencyValidation;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

@WebServlet({"/currencies", "/currencies/*"})
@Slf4j
public class CurrenciesServlet extends BaseServlet {
    private CurrencyService currencyService;

    @Override
    public void init() throws ServletException {
        currencyService = (CurrencyService) getServletContext().getAttribute(AppLifecycleListener.CURRENCY_SERVICE_ATTR);
        if (currencyService == null) {
            throw new ServletException("CurrencyService is not initialized");
        }
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        log.info("GET /currencies getAll()");
        List<CurrencyResponseDto> dtos = currencyService.getAll();
        if (dtos.isEmpty()) {
            sendSuccessResponse(resp, Collections.emptyList());
            return;
        }
        sendSuccessResponse(resp, dtos);
    }

    @Override
    public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");
        log.info("POST /currencies - received: name='{}', code='{}', sign='{}'",
                name != null ? name : "null",
                code != null ? code : "null",
                sign != null ? sign : "null");

        InputSecurityValidation.validateCurrencyNameWrite(name);
        CurrencyValidation.validateCurrency(name, code, sign);
        CreateCurrencyRequestDto requestDto = new CreateCurrencyRequestDto(name, code, sign);
        CurrencyResponseDto createdDto = currencyService.create(requestDto);
        log.info("Currency created successfully: code='{}'", code);
        sendCreationSuccessResponse(resp, createdDto);
    }

    @Override
    public void destroy() {
        log.info("Destroying Servlet 'CurrenciesServlet' — application is shutting down");
    }
}

package com.jb.currencyexchange.controller;

import com.jb.currencyexchange.dao.JdbcCurrencyDao;
import com.jb.currencyexchange.dto.response.CurrencyResponseDto;
import com.jb.currencyexchange.exception.ValidationException;
import com.jb.currencyexchange.mapper.CurrencyMapper;
import com.jb.currencyexchange.service.CurrencyService;
import com.jb.currencyexchange.util.PathUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@WebServlet("/currency/*")
@Slf4j
public class CurrencyServlet extends BaseServlet {
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
            String code = PathUtils.extractCurrencyCode(req);
            log.info("GET /currency/{}", code);
            if (code == null || code.trim().isEmpty()) {
                throw new ValidationException(
                        "Currency code is missing. Expected format: /currency/{CODE} (3 characters)"
                );
            }
            CurrencyResponseDto currency = currencyService.getByCode(code);
            sendSuccessResponse(resp, currency);
        } catch (Exception e) {
            handleException(resp, e);
        }
    }

    @Override
    public void destroy() {
        log.info("Destroying Servlet 'CurrencyServlet' — application is shutting down");
    }
}

package com.jb.currencyexchange.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(value = {"/currencies", "/currency/*", "/exchangeRate/*", "/exchangeRates", "/exchange"})
public class EncodingFilter extends HttpFilter {
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CHARSET_UTF8 = "UTF-8";
    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws IOException, ServletException {
        res.setCharacterEncoding(CHARSET_UTF8);
        req.setCharacterEncoding(CHARSET_UTF8);
        res.setContentType(CONTENT_TYPE_JSON);

        super.doFilter(req, res, chain);
    }
}

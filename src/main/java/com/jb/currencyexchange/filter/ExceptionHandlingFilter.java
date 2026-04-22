package com.jb.currencyexchange.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jb.currencyexchange.exception.ExceptionHandler;
import com.jb.currencyexchange.exception.ExceptionMessage;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@WebFilter("/*")
public class ExceptionHandlingFilter extends HttpFilter {
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExceptionHandler exceptionHandler = new ExceptionHandler();

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse res, FilterChain chain) throws ServletException, IOException {
        try {
            chain.doFilter(req, res);
        } catch (Exception e) {
            if (res.isCommitted()) {
                throw e;
            }
            writeErrorResponse(res, e);
        }
    }

    private void writeErrorResponse(HttpServletResponse response, Exception e) throws IOException {
        ExceptionMessage mapped = exceptionHandler.mapToMessage(e);
        String message = exceptionHandler.resolveClientMessage(e, mapped);
        Map<String, Object> error = new LinkedHashMap<>();
        error.put("code", mapped.getCode());
        error.put("message", message);
        error.put("status", mapped.getStatus());

        response.setStatus(mapped.getStatus());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        objectMapper.writeValue(response.getWriter(), error);
    }
}

package com.jb.currencyexchange.controller;

import com.jb.currencyexchange.util.JsonUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
public abstract class BaseServlet extends HttpServlet {
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CHARSET_UTF8 = "UTF-8";

    protected <T> void sendJsonResponse(HttpServletResponse resp, T payload, int status) throws IOException {
        resp.setContentType(CONTENT_TYPE_JSON);
        resp.setCharacterEncoding(CHARSET_UTF8);
        resp.setStatus(status);

        PrintWriter writer = resp.getWriter();
        String json = JsonUtils.writeJson(payload);
        writer.write(json);
        writer.flush();
    }

    protected <T> void sendSuccessResponse(HttpServletResponse resp, T payload) throws IOException {
        sendJsonResponse(resp, payload, HttpServletResponse.SC_OK);
    }

    protected <T> void sendCreationSuccessResponse(HttpServletResponse resp, T payload) throws IOException {
        sendJsonResponse(resp, payload, HttpServletResponse.SC_CREATED);
    }
}

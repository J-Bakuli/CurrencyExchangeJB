package com.jb.currencyexchange.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jb.currencyexchange.exception.ExceptionMessage;
import com.jb.currencyexchange.exception.ValidationException;
import com.jb.currencyexchange.mapper.ExceptionMapper;
import com.jb.currencyexchange.util.JsonUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public abstract class BaseServlet extends HttpServlet {
    private static final String CONTENT_TYPE_JSON = "application/json";
    private static final String CHARSET_UTF8 = "UTF-8";
    private final ExceptionMapper exceptionMapper = new ExceptionMapper();

    protected <T> void sendJsonResponse(HttpServletResponse resp, T payload, int status) throws IOException {
        setResponseHeaders(resp);
        resp.setStatus(status);

        try (PrintWriter writer = resp.getWriter()) {
            String json = JsonUtils.writeJson(payload);
            writer.write(json);
            writer.flush();
        } catch (RuntimeException e) {
            log.error("Failed to serialize JSON response", e);
            sendErrorResponse(resp, "Failed to serialize response", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    protected void sendErrorResponse(HttpServletResponse resp, String message, int status) {
        sendErrorResponse(resp, "error", message, status, null);
    }

    protected void sendErrorResponse(
            HttpServletResponse resp,
            String code,
            String message,
            int status,
            Map<String, Object> details
    ) {
        setResponseHeaders(resp);
        resp.setStatus(status);
        String safeMessage = message != null ? message : "Unknown error";
        String safeCode = code != null && !code.isBlank() ? code : "error";
        try (PrintWriter writer = resp.getWriter()) {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> error = new LinkedHashMap<>();
            error.put("code", safeCode);
            error.put("message", safeMessage);
            error.put("status", status);
            if (details != null && !details.isEmpty()) {
                error.put("details", details);
            }
            String jsonError = mapper.writeValueAsString(error);
            writer.write(jsonError);
            writer.flush();
        } catch (IOException e) {
            log.error("Failed to write error response to client", e);
        }
    }

    protected <T> void sendSuccessResponse(HttpServletResponse resp, T payload) throws IOException {
        sendJsonResponse(resp, payload, HttpServletResponse.SC_OK);
    }

    protected <T> void sendCreationSuccessResponse(HttpServletResponse resp, T payload) throws IOException {
        sendJsonResponse(resp, payload, HttpServletResponse.SC_CREATED);
    }

    private void setResponseHeaders(HttpServletResponse resp) {
        resp.setContentType(CONTENT_TYPE_JSON);
        resp.setCharacterEncoding(CHARSET_UTF8);
    }

    protected void handleException(HttpServletResponse resp, Exception e) {
        ExceptionMessage errorMsg = exceptionMapper.mapToMessage(e);
        if (errorMsg == null) {
            log.error("ExceptionMapper returned null for exception: {}", e.getClass().getSimpleName());
            errorMsg = ExceptionMessage.INTERNAL_ERROR;
        }
        log.warn("Handling exception: {} (Status: {}) - {}",
                e.getClass().getSimpleName(),
                errorMsg.getStatus(),
                errorMsg.getMessage());
        try {
            String responseMessage = resolveErrorMessage(e, errorMsg);
            sendErrorResponse(resp, errorMsg.getCode(), responseMessage, errorMsg.getStatus(), null);
        } catch (Exception writeError) {
            log.error("Failed to send error response to client", writeError);
            try {
                resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                resp.getWriter().write("{\"code\":\"internal_error\",\"message\":\"Internal server error\",\"status\":500}");
            } catch (IOException ioError) {
                log.error("Completely failed to send any error response", ioError);
            }
        }
    }

    private String resolveErrorMessage(Exception e, ExceptionMessage mappedError) {
        String detailedMessage = e.getMessage();
        if (detailedMessage == null || detailedMessage.trim().isEmpty()) {
            return mappedError.getMessage();
        }

        if (e instanceof ValidationException) {
            return detailedMessage;
        }

        return mappedError.getMessage();
    }
}

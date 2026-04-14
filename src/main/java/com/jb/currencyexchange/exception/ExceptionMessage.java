package com.jb.currencyexchange.exception;

import jakarta.servlet.http.HttpServletResponse;

public enum ExceptionMessage {
    // 400 Bad Request
    DATA_IS_INVALID("data_is_invalid", "Input data is invalid", HttpServletResponse.SC_BAD_REQUEST),

    // 404 Not Found
    NOT_FOUND("not_found", "Not found", HttpServletResponse.SC_NOT_FOUND),

    // 409 Conflict
    ALREADY_EXISTS("already_exists", "Already exists", HttpServletResponse.SC_CONFLICT),

    // 500 Internal server error
    INTERNAL_ERROR("internal_error", "Application error", HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final int status;

    ExceptionMessage(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public int getStatus() {
        return status;
    }
}

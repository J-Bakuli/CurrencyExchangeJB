package com.jb.currencyexchange.exception;

import jakarta.servlet.http.HttpServletResponse;

public enum ExceptionMessage {
    // 400 Bad Request
    EMPTY_FORM_FIELD("empty_form_field", "Required form field is missing", HttpServletResponse.SC_BAD_REQUEST),
    CODE_NOT_IN_ADDRESS("code_not_in_address", "Currency code is not in address", HttpServletResponse.SC_BAD_REQUEST),
    DATA_IS_INVALID("data_is_invalid", "Input data is invalid", HttpServletResponse.SC_BAD_REQUEST),
    BAD_REQUEST("bad_request", "Bad request", HttpServletResponse.SC_BAD_REQUEST),

    // 404 Not Found
    CURRENCY_NOT_FOUND("currency_not_found", "Currency not found", HttpServletResponse.SC_NOT_FOUND),
    RATE_NOT_FOUND("rate_not_found", "Rate not found", HttpServletResponse.SC_NOT_FOUND),
    PAIR_EXCHANGE_RATE_NOT_FOUND("pair_exchange_rate_not_found", "Currency pair exchange rate is not found", HttpServletResponse.SC_NOT_FOUND),

    // 409 Conflict
    ALREADY_EXISTS("already_exists", "Currency with this code already exists", HttpServletResponse.SC_CONFLICT),
    PAIR_ALREADY_EXISTS("pair_already_exists", "Currency pair already exists", HttpServletResponse.SC_CONFLICT),

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

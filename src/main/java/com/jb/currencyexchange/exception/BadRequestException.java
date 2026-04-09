package com.jb.currencyexchange.exception;

import java.util.Map;

public class BadRequestException extends RuntimeException {
    private final Map<String, Object> details;

    public BadRequestException(String message, Throwable cause) {
        this(message, cause, null);
    }

    public BadRequestException(String message) {
        this(message, null, null);
    }

    public BadRequestException(String message, Map<String, Object> details) {
        this(message, null, details);
    }

    public BadRequestException(String message, Throwable cause, Map<String, Object> details) {
        super(message, cause);
        this.details = details;
    }

    public Map<String, Object> getDetails() {
        return details;
    }
}

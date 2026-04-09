package com.jb.currencyexchange.exception.validation;

import lombok.Getter;

@Getter
public class ValidationException extends RuntimeException {
    private final String field;
    private final String message;

    public ValidationException(String field, String message) {
        super(message);
        this.field = field;
        this.message = message;
    }

    public ValidationException(String message) {
        this(null, message);
    }
}

package com.jb.currencyexchange.exception.update;

public class UpdateFailedException extends RuntimeException {
    public UpdateFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}

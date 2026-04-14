package com.jb.currencyexchange.exception;

public class EmptyFormFieldException extends ValidationException {
    public EmptyFormFieldException(String message) {
        super(message);
    }
}

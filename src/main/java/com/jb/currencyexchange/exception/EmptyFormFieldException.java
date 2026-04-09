package com.jb.currencyexchange.exception;

import com.jb.currencyexchange.exception.validation.ValidationException;

public class EmptyFormFieldException extends ValidationException {
    public EmptyFormFieldException(String message) {
        super(message);
    }
}

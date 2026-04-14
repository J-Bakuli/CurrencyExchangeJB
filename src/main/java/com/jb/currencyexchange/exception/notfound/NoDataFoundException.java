package com.jb.currencyexchange.exception.notfound;

import com.jb.currencyexchange.exception.NotFoundException;

public class NoDataFoundException extends NotFoundException {
    public NoDataFoundException(String message) {
        super(message);
    }
}

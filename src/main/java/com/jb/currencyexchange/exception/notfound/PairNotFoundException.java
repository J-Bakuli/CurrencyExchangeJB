package com.jb.currencyexchange.exception.notfound;

import com.jb.currencyexchange.exception.NotFoundException;

public class PairNotFoundException extends NotFoundException {
    public PairNotFoundException(String message) {
        super(message);
    }
}

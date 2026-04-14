package com.jb.currencyexchange.validation.business;

import com.jb.currencyexchange.dao.CurrencyDao;
import com.jb.currencyexchange.exception.NotFoundException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class CurrencyBusinessValidation {
    private final CurrencyDao currencyDao;

    public CurrencyBusinessValidation(CurrencyDao currencyDao) {
        this.currencyDao = currencyDao;
    }

    public void validateCodePresence(String code) {
        String normalizedCode = code == null ? null : code.trim().toUpperCase();
        if (normalizedCode == null || normalizedCode.isEmpty() || currencyDao.getByCode(normalizedCode).isEmpty()) {
            throw new NotFoundException(code);
        }
    }
}

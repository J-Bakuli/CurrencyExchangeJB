package com.jb.currencyexchange.dao;

import com.jb.currencyexchange.model.Currency;

import java.util.Optional;

public interface CurrencyDao extends BaseDao<Currency> {
    Optional<Currency> getByCode(String code);
}

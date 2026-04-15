package com.jb.currencyexchange.dao;

import com.jb.currencyexchange.model.ExchangeRate;

import java.util.Optional;

public interface ExchangeRateDao extends BaseDao<ExchangeRate> {
    Optional<ExchangeRate> getByCurrencyCodes(String baseCurrencyCode, String targetCurrencyCode);
    boolean existsByCurrencyPair(String baseCode, String targetCode);
}

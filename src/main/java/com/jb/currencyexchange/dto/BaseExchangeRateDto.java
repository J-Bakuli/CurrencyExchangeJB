package com.jb.currencyexchange.dto;

import java.math.BigDecimal;

public interface BaseExchangeRateDto {
    String baseCode();
    String targetCode();
    BigDecimal rate();
}

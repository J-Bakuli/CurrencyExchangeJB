package com.jb.currencyexchange.dto;

import com.jb.currencyexchange.dto.response.CurrencyResponseDto;

import java.math.BigDecimal;

public record ExchangeResultDto(
        CurrencyResponseDto baseCurrency,
        CurrencyResponseDto targetCurrency,
        BigDecimal rate,
        BigDecimal amount,
        BigDecimal convertedAmount
) {
}

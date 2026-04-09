package com.jb.currencyexchange.dto.request;

import com.jb.currencyexchange.dto.BaseExchangeRateDto;

import java.math.BigDecimal;

public record UpdateExchangeRateRequestDto(String baseCode, String targetCode, BigDecimal rate) implements
        BaseExchangeRateDto {
}
package com.jb.currencyexchange.dto.request;

import java.math.BigDecimal;

public record UpdateExchangeRateRequestDto(String baseCode, String targetCode, BigDecimal rate) {
}
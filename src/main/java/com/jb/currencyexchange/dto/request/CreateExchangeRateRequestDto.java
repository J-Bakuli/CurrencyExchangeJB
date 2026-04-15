package com.jb.currencyexchange.dto.request;

import java.math.BigDecimal;

public record CreateExchangeRateRequestDto(String baseCode, String targetCode, BigDecimal rate) {
}

package com.jb.currencyexchange.dto.request;

import com.jb.currencyexchange.dto.BaseCurrencyDto;

public record CreateCurrencyRequestDto(String name, String code, String sign) implements BaseCurrencyDto {
}
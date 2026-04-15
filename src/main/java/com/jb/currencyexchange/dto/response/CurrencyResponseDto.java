package com.jb.currencyexchange.dto.response;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "name", "code", "sign"})
public record CurrencyResponseDto(
        Integer id,
        String name,
        String code,
        String sign) {
}

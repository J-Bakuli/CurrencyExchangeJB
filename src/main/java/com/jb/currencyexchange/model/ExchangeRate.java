package com.jb.currencyexchange.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@AllArgsConstructor
public class ExchangeRate {
    private Integer id;
    private Currency baseCurrency;
    private Currency targetCurrency;
    private BigDecimal rate;
}

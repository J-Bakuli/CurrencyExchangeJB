package com.jb.currencyexchange.mapper;

import com.jb.currencyexchange.dto.request.CreateExchangeRateRequestDto;
import com.jb.currencyexchange.dto.response.ExchangeRateResponseDto;
import com.jb.currencyexchange.model.Currency;
import com.jb.currencyexchange.model.ExchangeRate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ExchangeRateMapper {
    ExchangeRateMapper INSTANCE = Mappers.getMapper(ExchangeRateMapper.class);

    @Mapping(target = "baseCurrency", expression = "java(toCurrency(dto.baseCode()))")
    @Mapping(target = "targetCurrency", expression = "java(toCurrency(dto.targetCode()))")
    ExchangeRate toEntity(CreateExchangeRateRequestDto dto);

    @Mapping(target = "baseCurrency", source = "baseCurrency")
    @Mapping(target = "targetCurrency", source = "targetCurrency")
    ExchangeRateResponseDto toResponseDto(ExchangeRate entity);

    default Currency toCurrency(String code) {
        if (code == null) {
            return null;
        }
        return new Currency(null, null, code.trim().toUpperCase(), null);
    }
}

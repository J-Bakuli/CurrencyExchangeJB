package com.jb.currencyexchange.mapper;

import com.jb.currencyexchange.dto.response.ExchangeRateResponseDto;
import com.jb.currencyexchange.model.ExchangeRate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ExchangeRateMapper {
    ExchangeRateMapper INSTANCE = Mappers.getMapper(ExchangeRateMapper.class);

    @Mapping(target = "baseCurrency", source = "baseCurrency")
    @Mapping(target = "targetCurrency", source = "targetCurrency")
    ExchangeRateResponseDto toResponseDto(ExchangeRate entity);
}

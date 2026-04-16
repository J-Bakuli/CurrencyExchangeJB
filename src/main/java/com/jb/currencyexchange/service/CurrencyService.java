package com.jb.currencyexchange.service;

import com.jb.currencyexchange.dao.CurrencyDao;
import com.jb.currencyexchange.dto.request.CreateCurrencyRequestDto;
import com.jb.currencyexchange.dto.response.CurrencyResponseDto;
import com.jb.currencyexchange.exception.AlreadyExistsException;
import com.jb.currencyexchange.exception.DatabaseException;
import com.jb.currencyexchange.exception.NotFoundException;
import com.jb.currencyexchange.exception.ValidationException;
import com.jb.currencyexchange.mapper.CurrencyMapper;
import com.jb.currencyexchange.model.Currency;
import com.jb.currencyexchange.validation.structural.DtoValidation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.List;

import static com.jb.currencyexchange.util.CommonValidationUtils.isUniqueConstraintViolation;

@Slf4j
public class CurrencyService {
    @Getter
    private final CurrencyDao currencyDao;
    private final CurrencyMapper mapper;

    public CurrencyService(CurrencyDao currencyDao, CurrencyMapper mapper) {
        this.currencyDao = currencyDao;
        this.mapper = mapper;
    }

    public List<CurrencyResponseDto> getAll() {
        return currencyDao.getAll()
                .stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    public CurrencyResponseDto create(CreateCurrencyRequestDto requestDto) {
        DtoValidation.validate(requestDto);
        try {
            log.debug("Mapping DTO to entity: code='{}', name='{}', sign='{}'", requestDto.code(), requestDto.name(), requestDto.sign());
            Currency currency = mapper.toEntity(requestDto);
            String normalizedCode = requestDto.code().trim().toUpperCase();
            currency.setCode(normalizedCode);

            log.debug("Saving currency to DB: name='{}', code='{}', sign='{}'", currency.getName(), currency.getCode(), currency.getSign());
            Currency savedCurrency = currencyDao.create(currency);

            log.info("Currency successfully created: id={}, name='{}', code='{}'",
                    savedCurrency.getId(), savedCurrency.getName(), savedCurrency.getCode());

            return mapper.toResponseDto(savedCurrency);
        } catch (AlreadyExistsException e) {
            log.warn("Attempt to create duplicate currency with code={}", requestDto.code());
            throw e;
        } catch (RuntimeException e) {
            if (e.getCause() instanceof SQLException sqlEx && isUniqueConstraintViolation(sqlEx)) {
                log.warn("Duplicate currency code detected at DB level: {}", requestDto.code(), e);
                throw new AlreadyExistsException(
                        String.format("Currency with code '%s' already exists", requestDto.code()), e);
            } else {
                log.error("Failed to create currency with code={}: {}",
                        requestDto.code(), e.getMessage(), e);
                throw new DatabaseException(
                        String.format("Failed to create currency with code=%s", requestDto.code()), e);
            }
        }
    }

    public CurrencyResponseDto getByCode(String code) {
        log.info("Currency to get by code: code={}", code);
        if (code == null || code.trim().isEmpty()) {
            throw new ValidationException("Currency code cannot be null or empty");
        }
        return currencyDao.getByCode(code)
                .map(mapper::toResponseDto)
                .orElseThrow(() -> {
                    log.warn("Currency not found for code: {}", code);
                    return new NotFoundException(
                            String.format("Currency is not found by code: code=%s", code));
                });
    }
}

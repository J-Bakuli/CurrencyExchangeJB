package com.jb.currencyexchange.service;

import com.jb.currencyexchange.dao.CurrencyDao;
import com.jb.currencyexchange.dao.ExchangeRateDao;
import com.jb.currencyexchange.dto.request.CreateExchangeRateRequestDto;
import com.jb.currencyexchange.dto.request.UpdateExchangeRateRequestDto;
import com.jb.currencyexchange.dto.response.ExchangeRateResponseDto;
import com.jb.currencyexchange.exception.AlreadyExistsException;
import com.jb.currencyexchange.exception.DatabaseException;
import com.jb.currencyexchange.exception.NotFoundException;
import com.jb.currencyexchange.exception.ValidationException;
import com.jb.currencyexchange.mapper.ExchangeRateMapper;
import com.jb.currencyexchange.model.Currency;
import com.jb.currencyexchange.model.ExchangeRate;
import com.jb.currencyexchange.validation.business.CurrencyBusinessValidation;
import com.jb.currencyexchange.validation.structural.CurrencyValidation;
import com.jb.currencyexchange.validation.structural.DtoValidation;
import com.jb.currencyexchange.validation.structural.ExchangeRateValidation;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class ExchangeRateService {
    @Getter
    private final ExchangeRateDao exchangeRateDao;
    private final CurrencyDao currencyDao;
    private final CurrencyBusinessValidation currencyBusinessValidation;
    private final ExchangeRateMapper mapper;

    public ExchangeRateService(ExchangeRateDao exchangeRateDao, CurrencyDao currencyDao,
                               CurrencyBusinessValidation currencyBusinessValidation, ExchangeRateMapper mapper) {
        this.exchangeRateDao = exchangeRateDao;
        this.currencyDao = currencyDao;
        this.currencyBusinessValidation = currencyBusinessValidation;
        this.mapper = mapper;
    }

    public ExchangeRateResponseDto create(CreateExchangeRateRequestDto request) {
        DtoValidation.validate(request);

        String baseCode = request.baseCode().trim().toUpperCase();
        String targetCode = request.targetCode().trim().toUpperCase();
        BigDecimal rate = request.rate();
        String pair = baseCode + "-" + targetCode;

        log.info("Creating exchange rate: {} → {} = {}", baseCode, targetCode, rate);

        try {
            Optional<Currency> baseCurrencyOpt = currencyDao.getByCode(baseCode);
            Optional<Currency> targetCurrencyOpt = currencyDao.getByCode(targetCode);

            List<String> missingCurrencies = new ArrayList<>();

            if (baseCurrencyOpt.isEmpty()) {
                missingCurrencies.add(baseCode);
            }
            if (targetCurrencyOpt.isEmpty()) {
                missingCurrencies.add(targetCode);
            }

            if (!missingCurrencies.isEmpty()) {
                throw new NotFoundException(
                        String.format("Currency not found for codes: %s", String.join(", ", missingCurrencies))
                );
            }

            Currency baseCurrency = baseCurrencyOpt.get();
            Currency targetCurrency = targetCurrencyOpt.get();

            if (baseCurrency.getCode() == null || baseCurrency.getCode().trim().isEmpty()) {
                log.error("Base currency code is invalid (null or empty): {}", baseCurrency);
                throw new NotFoundException(baseCode);
            }
            if (targetCurrency.getCode() == null || targetCurrency.getCode().trim().isEmpty()) {
                log.error("Target currency code is invalid (null or empty): {}", targetCurrency);
                throw new NotFoundException(targetCode);
            }

            ExchangeRate exchangeRate = mapper.toEntity(request);
            exchangeRate.setBaseCurrency(baseCurrency);
            exchangeRate.setTargetCurrency(targetCurrency);

            ExchangeRateValidation.validate(exchangeRate);
            ExchangeRate created = exchangeRateDao.create(exchangeRate);

            log.info("Successfully created exchange rate: {} → {} = {}, baseCurrencyCode={}, targetCurrencyCode={}",
                    created.getBaseCurrency(),
                    created.getTargetCurrency(),
                    created.getRate(),
                    baseCurrency.getCode(),
                    targetCurrency.getCode()
            );

            return mapper.toResponseDto(created);

        } catch (NotFoundException e) {
            log.warn("Currency not found for pair {}: {}", pair, e.getMessage());
            throw e;
        } catch (AlreadyExistsException e) {
            log.warn("Currency pair already exists for pair {}: {}", pair, e.getMessage());
            throw e;
        } catch (DatabaseException e) {
            log.error("Failed to create exchange rate for pair {}: {}", pair, e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            log.error("Unexpected error while creating exchange rate for pair {}: {}",
                    pair, e.getMessage(), e);
            throw new DatabaseException(
                    String.format("Unexpected error creating exchange rate for pair %s", pair), e
            );
        }
    }

    public ExchangeRateResponseDto getRate(String baseCode, String targetCode) {
        CurrencyValidation.validateCurrencyCodes(baseCode, targetCode);
        String pair = baseCode.toUpperCase() + "/" + targetCode.toUpperCase();
        log.info("Fetching exchange rate for pair {}", pair);

        try {
            Optional<ExchangeRate> rateOpt = exchangeRateDao.getByCurrencyCodes(baseCode, targetCode);
            if (rateOpt.isEmpty()) {
                log.warn("Exchange rate not found for pair {}", pair);
                throw new NotFoundException("Rate not found for pair " + pair);
            }
            currencyBusinessValidation.validateCodePresence(baseCode);
            currencyBusinessValidation.validateCodePresence(targetCode);

            ExchangeRate rate = rateOpt.get();
            ExchangeRateValidation.validate(rate);
            log.debug("Found exchange rate for {}→{}: {}", baseCode, targetCode, rate.getRate());
            return mapper.toResponseDto(rate);
        } catch (NotFoundException e) {
            log.warn("Not found: {}", e.getMessage());
            throw e;
        } catch (ValidationException e) {
            log.warn("Invalid exchange rate in DB for pair {}: {}", pair, e.getMessage());
            throw new ValidationException(
                    String.format("Corrupted data for pair %s: %s", pair, e.getMessage()), e);
        } catch (RuntimeException e) {
            log.error("Unexpected error fetching rate for pair {}: {}", pair, e.getMessage(), e);
            throw new DatabaseException(
                    String.format("Failed to retrieve rate for %s/%s", baseCode, targetCode), e);
        }
    }

    public List<ExchangeRateResponseDto> getAllRates() {
        return exchangeRateDao.getAll().stream()
                .map(mapper::toResponseDto)
                .toList();
    }

    public ExchangeRateResponseDto update(UpdateExchangeRateRequestDto request) {
        DtoValidation.validate(request);

        String baseCode = request.baseCode().trim().toUpperCase();
        String targetCode = request.targetCode().trim().toUpperCase();
        String pair = baseCode + "-" + targetCode;
        log.info("Starting update of exchange rate for pair {}", pair);

        try {
            CurrencyValidation.validateCurrencyCodes(baseCode, targetCode);

            Currency baseCurrency = currencyDao.getByCode(baseCode)
                    .orElseThrow(() -> new NotFoundException(
                            baseCode
                    ));
            Currency targetCurrency = currencyDao.getByCode(targetCode)
                    .orElseThrow(() -> new NotFoundException(
                            targetCode
                    ));

            Optional<ExchangeRate> existingRate = exchangeRateDao.getByCurrencyCodes(baseCode, targetCode);
            if (existingRate.isEmpty()) {
                log.warn("Exchange rate not found for pair {} (base ID: {}, target ID: {})",
                        pair, baseCurrency.getId(), targetCurrency.getId());
                throw new NotFoundException(
                        String.format("Exchange rate not found for currency pair: %s-%s", baseCode, targetCode)
                );
            }

            ExchangeRate rateToUpdate = existingRate.get();
            rateToUpdate.setRate(request.rate());
            rateToUpdate.setBaseCurrency(baseCurrency);
            rateToUpdate.setTargetCurrency(targetCurrency);

            ExchangeRate updatedRate = exchangeRateDao.update(rateToUpdate);
            log.info("Successfully updated exchange rate: {}→{} = {} (ID: {})",
                    updatedRate.getBaseCurrency(), updatedRate.getTargetCurrency(),
                    updatedRate.getRate(), updatedRate.getId());

            return mapper.toResponseDto(updatedRate);
        } catch (NotFoundException e) {
            log.warn("Not found for pair {}: {}", pair, e.getMessage());
            throw e;
        } catch (ValidationException e) {
            log.warn("Validation failed for exchange rate update (pair {}): {}", pair, e.getMessage());
            throw new ValidationException("Invalid exchange rate data: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Unexpected error while updating exchange rate pair {}: {}", pair, e.getMessage(), e);
            throw new DatabaseException(
                    String.format("Failed to update exchange rate for currency pair %s", pair), e
            );
        }
    }
}

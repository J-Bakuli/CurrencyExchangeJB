package com.jb.currencyexchange.service;

import com.jb.currencyexchange.dao.CurrencyDao;
import com.jb.currencyexchange.dao.ExchangeRateDao;
import com.jb.currencyexchange.dto.request.CreateExchangeRateRequestDto;
import com.jb.currencyexchange.dto.request.UpdateExchangeRateRequestDto;
import com.jb.currencyexchange.dto.response.ExchangeRateResponseDto;
import com.jb.currencyexchange.exception.BadRequestException;
import com.jb.currencyexchange.exception.ExchangeRateRetrievalException;
import com.jb.currencyexchange.exception.alreadyexists.CurrencyPairAlreadyExistsException;
import com.jb.currencyexchange.exception.creation.ExchangeRateCreationException;
import com.jb.currencyexchange.exception.notfound.CurrencyNotFoundException;
import com.jb.currencyexchange.exception.notfound.NoDataFoundException;
import com.jb.currencyexchange.exception.notfound.RateNotFoundException;
import com.jb.currencyexchange.exception.update.ExchangeRateUpdateException;
import com.jb.currencyexchange.exception.validation.ExchangeRateIntergityException;
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
            if (exchangeRateDao.existsByCurrencyPair(baseCode, targetCode)) {
                log.warn("Currency pair {} already exists", pair);
                throw new CurrencyPairAlreadyExistsException(baseCode, targetCode);
            }

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
                throw new CurrencyNotFoundException(
                        baseCurrencyOpt.isEmpty() ? baseCode : null,
                        targetCurrencyOpt.isEmpty() ? targetCode : null,
                        missingCurrencies
                );
            }

            Currency baseCurrency = baseCurrencyOpt.get();
            Currency targetCurrency = targetCurrencyOpt.get();

            if (baseCurrency.getCode() == null || baseCurrency.getCode().trim().isEmpty()) {
                log.error("Base currency code is invalid (null or empty): {}", baseCurrency);
                throw new CurrencyNotFoundException(baseCode, null, List.of(baseCode));
            }
            if (targetCurrency.getCode() == null || targetCurrency.getCode().trim().isEmpty()) {
                log.error("Target currency code is invalid (null or empty): {}", targetCurrency);
                throw new CurrencyNotFoundException(null, targetCode, List.of(targetCode));
            }

            ExchangeRate exchangeRate = mapper.toEntity(request);
            exchangeRate.setBaseCode(baseCurrency);
            exchangeRate.setTargetCode(targetCurrency);

            ExchangeRateValidation.validate(exchangeRate);
            ExchangeRate created = exchangeRateDao.create(exchangeRate);

            log.info("Successfully created exchange rate: {} → {} = {}, baseCurrencyCode={}, targetCurrencyCode={}",
                    created.getBaseCode(),
                    created.getTargetCode(),
                    created.getRate(),
                    baseCurrency.getCode(),
                    targetCurrency.getCode()
            );

            return mapper.toResponseDto(created);

        } catch (CurrencyNotFoundException e) {
            log.warn("Currency not found for pair {}: {}", pair, e.getMessage());
            throw e;
        } catch (CurrencyPairAlreadyExistsException e) {
            log.warn("Currency pair already exists for pair {}: {}", pair, e.getMessage());
            throw e;
        } catch (ExchangeRateCreationException e) {
            log.error("Failed to create exchange rate for pair {}: {}", pair, e.getMessage(), e);
            throw e;
        } catch (RuntimeException e) {
            log.error("Unexpected error while creating exchange rate for pair {}: {}",
                    pair, e.getMessage(), e);
            throw new ExchangeRateCreationException(
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
                throw new RateNotFoundException("Rate not found for pair " + pair);
            }
            currencyBusinessValidation.validateCodePresence(baseCode);
            currencyBusinessValidation.validateCodePresence(targetCode);

            ExchangeRate rate = rateOpt.get();
            ExchangeRateValidation.validate(rate);
            log.debug("Found exchange rate for {}→{}: {}", baseCode, targetCode, rate.getRate());
            return mapper.toResponseDto(rate);
        } catch (CurrencyNotFoundException e) {
            log.warn("Currency not found: {}", e.getMessage());
            throw e;
        } catch (RateNotFoundException e) {
            log.warn("Rate not found: {}", e.getMessage());
            throw e;
        } catch (ValidationException e) {
            log.error("Invalid exchange rate in DB for pair {}: {}", pair, e.getMessage());
            throw new ExchangeRateIntergityException(
                    String.format("Corrupted data for pair %s: %s", pair, e.getMessage()), e);
        } catch (RuntimeException e) {
            log.error("Unexpected error fetching rate for pair {}: {}", pair, e.getMessage(), e);
            throw new ExchangeRateRetrievalException(
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
                    .orElseThrow(() -> new CurrencyNotFoundException(
                            baseCode,
                            null,
                            List.of(baseCode)
                    ));
            Currency targetCurrency = currencyDao.getByCode(targetCode)
                    .orElseThrow(() -> new CurrencyNotFoundException(
                            null,
                            targetCode,
                            List.of(targetCode)
                    ));

            Optional<ExchangeRate> existingRate = exchangeRateDao.getByCurrencyCodes(baseCode, targetCode);
            if (existingRate.isEmpty()) {
                log.warn("Exchange rate not found for pair {} (base ID: {}, target ID: {})",
                        pair, baseCurrency.getId(), targetCurrency.getId());
                throw new NoDataFoundException(
                        String.format("Exchange rate not found for currency pair: %s-%s", baseCode, targetCode)
                );
            }

            ExchangeRate rateToUpdate = existingRate.get();
            rateToUpdate.setRate(request.rate());
            rateToUpdate.setBaseCode(baseCurrency);
            rateToUpdate.setTargetCode(targetCurrency);

            ExchangeRate updatedRate = exchangeRateDao.update(rateToUpdate);
            log.info("Successfully updated exchange rate: {}→{} = {} (ID: {})",
                    updatedRate.getBaseCode(), updatedRate.getTargetCode(),
                    updatedRate.getRate(), updatedRate.getId());

            return mapper.toResponseDto(updatedRate);
        } catch (CurrencyNotFoundException e) {
            log.error("Currency not found for pair {}: {}", pair, e.getMessage());
            throw e;
        } catch (NoDataFoundException e) {
            log.warn("Exchange rate not found for update: pair {}", pair);
            throw e;
        } catch (ValidationException e) {
            log.error("Validation failed for exchange rate update (pair {}): {}", pair, e.getMessage());
            throw new BadRequestException("Invalid exchange rate data: " + e.getMessage(), e);
        } catch (RuntimeException e) {
            log.error("Unexpected error while updating exchange rate pair {}: {}", pair, e.getMessage(), e);
            throw new ExchangeRateUpdateException(
                    String.format("Failed to update exchange rate for currency pair %s", pair), e
            );
        }
    }
}

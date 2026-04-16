package com.jb.currencyexchange.service;

import com.jb.currencyexchange.dao.CurrencyDao;
import com.jb.currencyexchange.dao.ExchangeRateDao;
import com.jb.currencyexchange.dto.ExchangeResultDto;
import com.jb.currencyexchange.dto.response.CurrencyResponseDto;
import com.jb.currencyexchange.exception.NotFoundException;
import com.jb.currencyexchange.exception.ValidationException;
import com.jb.currencyexchange.model.Currency;
import com.jb.currencyexchange.model.ExchangeRate;
import com.jb.currencyexchange.util.CommonValidationUtils;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

@Slf4j
public class ExchangeRateCalculatorService {
    private final CurrencyDao currencyDao;
    private final ExchangeRateDao exchangeRateDao;
    private static final int ROUND_SCALE = 6;
    private static final int AMOUNT_SCALE = 2;

    public ExchangeRateCalculatorService(CurrencyDao currencyDao, ExchangeRateDao exchangeRateDao) {
        this.currencyDao = currencyDao;
        this.exchangeRateDao = exchangeRateDao;
    }

    public ExchangeResultDto calculate(String fromCode, String toCode, BigDecimal amount) {
        CommonValidationUtils.validateExchangeRequestParams(fromCode, toCode, amount);

        Currency from = getCurrencyOrThrow(fromCode);
        Currency to = getCurrencyOrThrow(toCode);

        if (fromCode.equals(toCode)) {
            return createResultDto(from, to, BigDecimal.ONE, amount);
        }

        Optional<ExchangeRate> directRateOpt = exchangeRateDao.getByCurrencyCodes(fromCode, toCode);
        if (directRateOpt.isPresent()) {
            ExchangeRate rate = directRateOpt.get();
            return createResultDto(from, to, rate.getRate(), amount);
        }

        Optional<ExchangeRate> reverseRateOpt = exchangeRateDao.getByCurrencyCodes(toCode, fromCode);
        if (reverseRateOpt.isPresent()) {
            BigDecimal directRate = calculateInverseRate(reverseRateOpt.get().getRate());
            return createResultDto(from, to, directRate, amount);
        }

        String baseCode = "USD";
        if (!fromCode.equals(baseCode) && !toCode.equals(baseCode)) {
            Optional<ExchangeRate> usdToFrom = exchangeRateDao.getByCurrencyCodes(baseCode, fromCode);
            Optional<ExchangeRate> usdToTo = exchangeRateDao.getByCurrencyCodes(baseCode, toCode);

            if (usdToFrom.isPresent() && usdToTo.isPresent()) {
                BigDecimal rate = usdToTo.get().getRate()
                        .divide(usdToFrom.get().getRate(), ROUND_SCALE, RoundingMode.HALF_UP);
                return createResultDto(from, to, rate, amount);
            }
        }
        throw new NotFoundException(String.format("No exchange rate found for %s→%s", fromCode, toCode));
    }

    private Currency getCurrencyOrThrow(String code) {
        return currencyDao.getByCode(code)
                .orElseThrow(() -> new NotFoundException("Currency " + code + " not found"));
    }

    private BigDecimal calculateInverseRate(BigDecimal rate) {
        if (rate.compareTo(BigDecimal.ZERO) == 0) {
            throw new ValidationException("Cannot calculate inverse of zero rate");
        }
        return BigDecimal.ONE.divide(rate, ROUND_SCALE, RoundingMode.HALF_UP);
    }

    private ExchangeResultDto createResultDto(Currency from, Currency to, BigDecimal rate, BigDecimal amount) {
        BigDecimal converted = amount.multiply(rate).setScale(AMOUNT_SCALE, RoundingMode.HALF_UP);
        CurrencyResponseDto baseCurrencyDto = new CurrencyResponseDto(
                from.getId(),
                from.getName(),
                from.getCode(),
                from.getSign()
        );

        CurrencyResponseDto targetCurrencyDto = new CurrencyResponseDto(
                to.getId(),
                to.getName(),
                to.getCode(),
                to.getSign()
        );

        return new ExchangeResultDto(
                baseCurrencyDto,
                targetCurrencyDto,
                rate,
                amount,
                converted
        );
    }
}

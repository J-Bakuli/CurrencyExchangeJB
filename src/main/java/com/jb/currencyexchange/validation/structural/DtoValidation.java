package com.jb.currencyexchange.validation.structural;

import com.jb.currencyexchange.dto.request.CreateCurrencyRequestDto;
import com.jb.currencyexchange.dto.request.CreateExchangeRateRequestDto;
import com.jb.currencyexchange.dto.request.UpdateExchangeRateRequestDto;
import com.jb.currencyexchange.util.CommonValidationUtils;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.jb.currencyexchange.util.CommonValidationUtils.*;

@Slf4j
public class DtoValidation {
    public static void validate(CreateCurrencyRequestDto dto) {
        log.debug("Validating DTO: code='{}', name='{}', sign='{}'", dto.code(), dto.name(), dto.sign());
        List<String> errors = new ArrayList<>();

        validateFields(Map.of(
                "code", dto.code(),
                "name", dto.name(),
                "sign", dto.sign()
        ), (value, fieldName) -> {
            if ("code".equals(fieldName)) {
                CurrencyValidation.validateCurrencyCode(value);
            } else if ("name".equals(fieldName)) {
                validateString(value, fieldName, 1, NAME_MAX_LENGTH, null, null, errors);
            } else if ("sign".equals(fieldName)) {
                validateString(value, fieldName, SIGN_MIN_LENGTH, SIGN_MAX_LENGTH, SIGN_PATTERN,
                        "must contain only letters or currency symbols (no spaces or punctuation)", errors);
            }
        });

        throwValidationExceptionIfErrors(errors);
    }

    public static void validate(CreateExchangeRateRequestDto dto) {
        List<String> errors = new ArrayList<>();
        CurrencyValidation.validateCurrencyCode(dto.baseCode());
        CurrencyValidation.validateCurrencyCode(dto.targetCode());
        validateNumber(dto.rate(), "rate", RATE_MAX_INTEGER_DIGITS, RATE_MAX_FRACTION_DIGITS, errors);
        throwValidationExceptionIfErrors(errors);
    }

    public static void validate(UpdateExchangeRateRequestDto dto) {
        List<String> errors = new ArrayList<>();

        if (dto.rate() == null) {
            errors.add("Rate cannot be null");
        } else if (dto.rate().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("Rate must be greater than zero");
        } else if (dto.rate().scale() > 6) {
            errors.add("Rate precision cannot exceed 6 decimal places");
        }

        CommonValidationUtils.throwValidationExceptionIfErrors(errors);
    }
}

package com.jb.currencyexchange.mapper;

import com.jb.currencyexchange.exception.BadRequestException;
import com.jb.currencyexchange.exception.EmptyFormFieldException;
import com.jb.currencyexchange.exception.ExceptionMessage;
import com.jb.currencyexchange.exception.ExchangeRateRetrievalException;
import com.jb.currencyexchange.exception.alreadyexists.AlreadyExistsException;
import com.jb.currencyexchange.exception.alreadyexists.CurrencyPairAlreadyExistsException;
import com.jb.currencyexchange.exception.notfound.CurrencyNotFoundException;
import com.jb.currencyexchange.exception.notfound.PairNotFoundException;
import com.jb.currencyexchange.exception.notfound.RateNotFoundException;
import com.jb.currencyexchange.exception.validation.ExchangeRateIntergityException;
import com.jb.currencyexchange.exception.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExceptionMapper {
    public ExceptionMessage mapToMessage(Exception e) {
        log.debug("Mapping exception: {} - {}", e.getClass().getSimpleName(), e.getMessage());

        if (e instanceof BadRequestException) {
            log.warn("Mapped {} to BAD_REQUEST (400): {}", e.getClass().getSimpleName(), e.getMessage());
            return ExceptionMessage.BAD_REQUEST;
        } else if (e instanceof RateNotFoundException) {
            log.warn("Mapped {} to RATE_NOT_FOUND (404): {}", e.getClass().getSimpleName(), e.getMessage());
            return ExceptionMessage.RATE_NOT_FOUND;
        } else if (e instanceof ExchangeRateRetrievalException) {
            log.warn("Mapped {} to INTERNAL_ERROR (500): {}", e.getClass().getSimpleName(), e.getMessage());
            return ExceptionMessage.INTERNAL_ERROR;
        } else if (e instanceof CurrencyNotFoundException) {
            log.warn("Mapped {} to CURRENCY_NOT_FOUND (404): {}", e.getClass().getSimpleName(), e.getMessage());
            return ExceptionMessage.CURRENCY_NOT_FOUND;
        } else if (e instanceof PairNotFoundException) {
            log.warn("Mapped {} to PAIR_EXCHANGE_RATE_NOT_FOUND (404): {}", e.getClass().getSimpleName(), e.getMessage());
            return ExceptionMessage.PAIR_EXCHANGE_RATE_NOT_FOUND;
        } else if (e instanceof EmptyFormFieldException) {
            log.warn("Mapped {} to EMPTY_FORM_FIELD (400): {}", e.getClass().getSimpleName(), e.getMessage());
            return ExceptionMessage.EMPTY_FORM_FIELD;
        } else if (e instanceof ExchangeRateIntergityException) {
            log.warn("Mapped {} to BAD_REQUEST (400): {}", e.getClass().getSimpleName(), e.getMessage());
            return ExceptionMessage.BAD_REQUEST;
        } else if (e instanceof AlreadyExistsException) {
            log.warn("Mapped {} to ALREADY_EXISTS (409): {}", e.getClass().getSimpleName(), e.getMessage());
            return ExceptionMessage.ALREADY_EXISTS;
        } else if (e instanceof CurrencyPairAlreadyExistsException) {
            log.warn("Mapped {} to PAIR_ALREADY_EXISTS (409): {}", e.getClass().getSimpleName(), e.getMessage());
            return ExceptionMessage.PAIR_ALREADY_EXISTS;
        } else if (e instanceof ValidationException || e instanceof IllegalArgumentException) {
            log.warn("Mapped {} to DATA_IS_INVALID (400): {}", e.getClass().getSimpleName(), e.getMessage());
            return ExceptionMessage.DATA_IS_INVALID;
        } else if (e instanceof NullPointerException) {
            log.warn("NullPointerException occurred — check null safety in business logic");
            log.warn("Mapped {} to DATA_IS_INVALID (400): {}", e.getClass().getSimpleName(), e.getMessage());
            return ExceptionMessage.DATA_IS_INVALID;
        } else {
            log.error("Unhandled exception type: {}", e.getClass().getSimpleName(), e);
            return ExceptionMessage.INTERNAL_ERROR;
        }
    }
}

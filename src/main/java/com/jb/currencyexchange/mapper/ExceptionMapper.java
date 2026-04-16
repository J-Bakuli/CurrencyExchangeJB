package com.jb.currencyexchange.mapper;

import com.jb.currencyexchange.exception.AlreadyExistsException;
import com.jb.currencyexchange.exception.DatabaseException;
import com.jb.currencyexchange.exception.ExceptionMessage;
import com.jb.currencyexchange.exception.NotFoundException;
import com.jb.currencyexchange.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ExceptionMapper {
    public ExceptionMessage mapToMessage(Exception e) {
        log.debug("Mapping exception: {} - {}", e.getClass().getSimpleName(), e.getMessage());

        if (e instanceof NotFoundException) {
            log.warn("Mapped {} to NOT_FOUND (404): {}", e.getClass().getSimpleName(), e.getMessage());
            return ExceptionMessage.NOT_FOUND;
        } else if (e instanceof AlreadyExistsException) {
            log.warn("Mapped {} to ALREADY_EXISTS (409): {}", e.getClass().getSimpleName(), e.getMessage());
            return ExceptionMessage.ALREADY_EXISTS;
        } else if (e instanceof ValidationException || e instanceof IllegalArgumentException) {
            log.warn("Mapped {} to DATA_IS_INVALID (400): {}", e.getClass().getSimpleName(), e.getMessage());
            return ExceptionMessage.DATA_IS_INVALID;
        } else if (e instanceof DatabaseException) {
            log.error("Mapped {} to INTERNAL_ERROR (500): {}", e.getClass().getSimpleName(), e.getMessage(), e);
            return ExceptionMessage.INTERNAL_ERROR;
        } else {
            log.error("Unhandled exception type: {}", e.getClass().getSimpleName(), e);
            return ExceptionMessage.INTERNAL_ERROR;
        }
    }
}

package com.jb.currencyexchange.parser;

import lombok.Getter;

import java.util.List;

@Getter
public class ParseResult <T> {
    private final boolean success;
    private final T data;
    private final String errorMessage;
    private final List<String> missingFields;

    private ParseResult(boolean success, T data, String errorMessage, List<String> missingFields) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.missingFields = missingFields != null ? missingFields : List.of();
    }

    public static <T> ParseResult<T> success(T data) {
        return new ParseResult<>(true, data, null, null);
    }

    public static <T> ParseResult<T> error(String errorMessage, List<String> missingFields) {
        return new ParseResult<>(false, null, errorMessage, missingFields);
    }
}
package com.jb.currencyexchange.parser;

public class ParseResult<T> {
    private final boolean success;
    private final T data;
    private final String errorMessage;

    private ParseResult(boolean success, T data, String errorMessage) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
    }

    public static <T> ParseResult<T> success(T data) {
        return new ParseResult<>(true, data, null);
    }

    public static <T> ParseResult<T> error(String errorMessage) {
        return new ParseResult<>(false, null, errorMessage);
    }

    public boolean isSuccess() {
        return success;
    }

    public T getData() {
        return data;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}

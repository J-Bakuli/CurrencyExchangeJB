package com.jb.currencyexchange.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jb.currencyexchange.exception.json.JsonSerializationException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public final class JsonUtils {
    private static final ObjectMapper mapper = new ObjectMapper();

    private JsonUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static <T> String writeJson(T obj) {
        if (obj == null) {
            return "null";
        }

        try {
            return mapper.writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            String errorMsg = "Failed to serialize object to JSON (type: %s)";
            log.error(String.format(errorMsg, obj.getClass().getSimpleName()));
            throw new JsonSerializationException(errorMsg, e);
        } catch (Exception e) {
            String errorMsg = "Unexpected error during JSON serialization";
            log.error(errorMsg, e);
            throw new JsonSerializationException(errorMsg, e);
        }
    }
}
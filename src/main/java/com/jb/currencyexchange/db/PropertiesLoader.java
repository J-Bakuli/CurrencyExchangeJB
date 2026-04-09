package com.jb.currencyexchange.db;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
    public static Properties loadProperties(String filename) throws IOException {
        Properties props = new Properties();
        try (InputStream input = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(filename)) {
            if (input == null) {
                throw new IOException("File not found: " + filename);
            }
            props.load(input);
        }
        return props;
    }
}

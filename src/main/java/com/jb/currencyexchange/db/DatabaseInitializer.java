package com.jb.currencyexchange.db;

import com.jb.currencyexchange.exception.ValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DatabaseInitializer {
    private static final List<String> SCRIPTS = List.of(
            "db/createTables.sql",
            "db/populateTables.sql"
    );

    public static void init(Connection conn) {
        for (String script : SCRIPTS) {
            executeSqlScript(conn, script);
        }
    }

    private static void executeSqlScript(Connection conn, String classpathResource) {
        try (Statement stmt = conn.createStatement()) {
            for (String sql : readResourceAsString(classpathResource).split(";")) {
                String statement = sql.trim();
                if (!statement.isEmpty()) {
                    stmt.execute(statement);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to execute script: " + classpathResource, e);
        }
    }

    private static String readResourceAsString(String resourcePath) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new ValidationException("Resource not found: " + resourcePath);
            }
            return new String(in.readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Failed to read resource: " + resourcePath, e);
        }
    }
}

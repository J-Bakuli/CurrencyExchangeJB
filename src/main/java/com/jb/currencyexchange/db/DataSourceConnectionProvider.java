package com.jb.currencyexchange.db;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
public class DataSourceConnectionProvider {
    private static final HikariConfig HIKARI_CONFIG;
    private static final HikariDataSource HIKARI_DATA_SOURCE;

    static {
        try {
            HIKARI_CONFIG = new HikariConfig(PropertiesLoader.loadProperties("datasource.properties"));
            String jdbcUrlOverride = System.getenv("JDBC_URL");
            if (jdbcUrlOverride != null && !jdbcUrlOverride.isBlank()) {
                HIKARI_CONFIG.setJdbcUrl(jdbcUrlOverride);
                log.info("Using JDBC_URL from environment");
            }
            HIKARI_CONFIG.setMaximumPoolSize(5);
            HIKARI_CONFIG.setMinimumIdle(2);
            HIKARI_CONFIG.setConnectionTimeout(30000);
            HIKARI_CONFIG.setIdleTimeout(600000);
            HIKARI_CONFIG.addDataSourceProperty("journal_mode", "WAL");
            HIKARI_CONFIG.addDataSourceProperty("synchronous", "NORMAL");

            HIKARI_DATA_SOURCE = new HikariDataSource(HIKARI_CONFIG);
            initializeDatabase();
            registerShutdownHook();
        } catch (IOException e) {
            throw new RuntimeException("Failed to load datasource properties", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        Connection connection = HIKARI_DATA_SOURCE.getConnection();

        try {
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON;");
            }
        } catch (SQLException e) {
            log.warn("Failed to set foreign keys ON, continuing with connection", e);
        }
        return connection;
    }

    private static void initializeDatabase() {
        try (Connection conn = getConnection()) {
            DatabaseInitializer.init(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }

    private static void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutdown hook triggered, closing HikariCP pool");
            if (!HIKARI_DATA_SOURCE.isClosed()) {
                HIKARI_DATA_SOURCE.close();
                log.info("HikariCP connection pool closed successfully");
            } else {
                log.debug("HikariCP pool was already closed");
            }
        }));
    }
}

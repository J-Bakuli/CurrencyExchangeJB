package com.jb.currencyexchange.db;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@WebListener
public class AppLifecycleListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try (Connection conn = DataSourceConnectionProvider.getConnection()) {
            DatabaseInitializer.init(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }
    }
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        log.info("Context is destroyed, closing HikariCP pool");
        try {
            DataSourceConnectionProvider.close();
        } catch (Exception e) {
            log.error("Failed to close datasource during shutdown", e);
        }
    }
}

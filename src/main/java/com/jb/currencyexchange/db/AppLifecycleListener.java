package com.jb.currencyexchange.db;

import com.jb.currencyexchange.dao.CurrencyDao;
import com.jb.currencyexchange.dao.ExchangeRateDao;
import com.jb.currencyexchange.dao.JdbcCurrencyDao;
import com.jb.currencyexchange.dao.JdbcExchangeRateDao;
import com.jb.currencyexchange.mapper.CurrencyMapper;
import com.jb.currencyexchange.mapper.ExchangeRateMapper;
import com.jb.currencyexchange.service.CurrencyService;
import com.jb.currencyexchange.service.ExchangeRateCalculatorService;
import com.jb.currencyexchange.service.ExchangeRateService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.SQLException;

@Slf4j
@WebListener
public class AppLifecycleListener implements ServletContextListener {
    public static final String CURRENCY_SERVICE_ATTR = "currencyService";
    public static final String EXCHANGE_RATE_SERVICE_ATTR = "exchangeRateService";
    public static final String EXCHANGE_CALCULATOR_SERVICE_ATTR = "exchangeCalculatorService";
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try (Connection conn = DataSourceConnectionProvider.getConnection()) {
            DatabaseInitializer.init(conn);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database", e);
        }

        CurrencyDao currencyDao = new JdbcCurrencyDao();
        ExchangeRateDao exchangeRateDao = new JdbcExchangeRateDao();

        CurrencyMapper currencyMapper = CurrencyMapper.INSTANCE;
        ExchangeRateMapper exchangeRateMapper = ExchangeRateMapper.INSTANCE;

        CurrencyService currencyService = new CurrencyService(currencyDao, currencyMapper);
        ExchangeRateService exchangeRateService = new ExchangeRateService(exchangeRateDao, currencyDao, exchangeRateMapper);
        ExchangeRateCalculatorService exchangeRateCalculatorService = new ExchangeRateCalculatorService(currencyDao, exchangeRateDao);

        ServletContext servletContext = sce.getServletContext();
        servletContext.setAttribute(CURRENCY_SERVICE_ATTR, currencyService);
        servletContext.setAttribute(EXCHANGE_RATE_SERVICE_ATTR, exchangeRateService);
        servletContext.setAttribute(EXCHANGE_CALCULATOR_SERVICE_ATTR, exchangeRateCalculatorService);
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

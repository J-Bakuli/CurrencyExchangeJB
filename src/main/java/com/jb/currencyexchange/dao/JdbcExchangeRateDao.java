package com.jb.currencyexchange.dao;

import com.jb.currencyexchange.db.DataSourceConnectionProvider;
import com.jb.currencyexchange.exception.AlreadyExistsException;
import com.jb.currencyexchange.exception.DatabaseException;
import com.jb.currencyexchange.exception.NotFoundException;
import com.jb.currencyexchange.exception.ValidationException;
import com.jb.currencyexchange.model.Currency;
import com.jb.currencyexchange.model.ExchangeRate;
import com.jb.currencyexchange.validation.structural.ExchangeRateValidation;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
public class JdbcExchangeRateDao implements ExchangeRateDao {
    private static final String CREATE = "INSERT INTO exchange_rate (base_currency_id, target_currency_id, rate) " +
            "VALUES (?, ?, ?)";
    private static final String SELECT_BY_CURRENCY_CODES = "SELECT er.id, er.rate, " +
            "c1.id as base_currency_id, c1.name as base_name, c1.code as base_code, c1.sign as base_sign, " +
            "c2.id as target_currency_id, c2.name as target_name, c2.code as target_code, c2.sign as target_sign " +
            "FROM exchange_rate er " + "JOIN currency c1 ON er.base_currency_id = c1.id " +
            "JOIN currency c2 ON er.target_currency_id = c2.id " + "WHERE c1.code = ? AND c2.code = ?";
    private static final String SELECT_ALL_WITH_JOIN = "SELECT er.id, er.rate, " +
            "c1.id as base_currency_id, c1.name as base_name, c1.code as base_code, c1.sign as base_sign, " +
            "c2.id as target_currency_id, c2.name as target_name, c2.code as target_code, c2.sign as target_sign " +
            "FROM exchange_rate er " + "JOIN currency c1 ON er.base_currency_id = c1.id " +
            "JOIN currency c2 ON er.target_currency_id = c2.id";
    private static final String UPDATE = "UPDATE exchange_rate SET rate = ? " + "WHERE id = ?";

    @Override
    public ExchangeRate create(ExchangeRate rate) {
        ExchangeRateValidation.validate(rate);
        Currency baseCurrency = rate.getBaseCurrency();
        Currency targetCurrency = rate.getTargetCurrency();
        log.debug("Creating exchange rate: {} -> {} = {}", baseCurrency.getCode(), targetCurrency.getCode(), rate.getRate());
        try (Connection connection = DataSourceConnectionProvider.getConnection(); PreparedStatement ps =
                connection.prepareStatement(CREATE, new String[]{"id"})) {
            Integer baseCurrencyId = baseCurrency.getId();
            Integer targetCurrencyId = targetCurrency.getId();
            validateCurrencyIds(baseCurrency, targetCurrency);
            ps.setInt(1, baseCurrencyId);
            ps.setInt(2, targetCurrencyId);
            ps.setBigDecimal(3, rate.getRate());
            ps.executeUpdate();

            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    rate.setId(generatedKeys.getInt(1));
                    log.debug("Successfully created exchange rate with ID: {}", rate.getId());
                } else {
                    throw new DatabaseException("No generated ID returned for exchange rate: " + rate);
                }
            }

            return rate;
        } catch (SQLException e) {
            if (isUniqueConstraintViolation(e)) {
                log.warn("Currency pair already exists: {}-{}",
                        rate.getBaseCurrency().getCode(), rate.getTargetCurrency().getCode());
                throw new AlreadyExistsException(
                        String.format("Exchange rate already exists for baseCode=%s, targetCode=%s",
                                baseCurrency.getCode(), targetCurrency.getCode()));
            } else {
                throw new DatabaseException("Failed to create exchange rate", e);
            }
        }
    }

    @Override
    public List<ExchangeRate> getAll() {
        log.debug("Executing getAll() - fetching all exchange rates with JOIN");
        try (Connection connection = DataSourceConnectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL_WITH_JOIN)) {
            ResultSet rs = ps.executeQuery();
            List<ExchangeRate> exchangeRates = new ArrayList<>();
            int count = 0;
            while (rs.next()) {
                count++;
                ExchangeRate rate = createFrom(rs);
                log.debug("Mapped exchange rate #{}: id={}, baseCode={}, targetCode={}, rate={}", count, rate.getId(),
                        rate.getBaseCurrency().getCode(), rate.getTargetCurrency().getCode(), rate.getRate());
                exchangeRates.add(rate);
            }
            log.info("Successfully fetched {} exchange rates", count);
            return exchangeRates;
        } catch (SQLException e) {
            log.error("Error executing query: {}", SELECT_ALL_WITH_JOIN, e);
            throw new DatabaseException("Error executing query: " + SELECT_ALL_WITH_JOIN, e);
        }
    }

    @Override
    public ExchangeRate update(ExchangeRate rate) {
        ExchangeRateValidation.validate(rate);
        Currency baseCurrency = rate.getBaseCurrency();
        Currency targetCurrency = rate.getTargetCurrency();
        log.debug("Updating exchange rate: ID={}, {} -> {} = {}", rate.getId(), baseCurrency.getCode(),
                targetCurrency.getCode(), rate.getRate());
        if (rate.getId() <= 0) {
            throw new ValidationException("Exchange rate ID must be positive for update.");
        }
        try (Connection connection = DataSourceConnectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE)) {
            ps.setBigDecimal(1, rate.getRate());
            ps.setInt(2, rate.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new NotFoundException(String.format("No exchange rate found with ID: %d. " +
                        "The record may have been deleted or never existed.", rate.getId()));
            }
            log.debug("Successfully updated exchange rate ID: {}", rate.getId());
            return rate;
        } catch (SQLException e) {
            log.error("Failed to update exchange rate ID={}: {}", rate.getId(), e.getMessage(), e);
            throw new DatabaseException(
                    String.format("Error updating exchange rate with ID=%d: %s", rate.getId(), e.getMessage()), e);
        }
    }

    @Override
    public Optional<ExchangeRate> getByCurrencyCodes(String baseCurrencyCode, String targetCurrencyCode) {
        return executeQuery(SELECT_BY_CURRENCY_CODES, new Object[]{baseCurrencyCode, targetCurrencyCode}, rs -> {
            try {
                return createFrom(rs);
            } catch (SQLException e) {
                throw new DatabaseException("Error mapping ResultSet to ExchangeRate", e);
            }
        });
    }

    private <T> Optional<T> executeQuery(String sql, Object[] paramValues, Function<ResultSet, T> rowMapper) {
        try (Connection connection = DataSourceConnectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            for (int i = 0; i < paramValues.length; i++) {
                setParameter(ps, i + 1, paramValues[i]);
            }
            ResultSet rs = ps.executeQuery();
            return rs.next() ? Optional.of(rowMapper.apply(rs)) : Optional.empty();
        } catch (SQLException e) {
            log.error("Error executing query: {}", sql, e);
            throw new DatabaseException("Error executing query: " + sql, e);
        }
    }

    private void setParameter(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value == null) {
            ps.setNull(index, Types.OTHER);
        } else if (value instanceof Long) {
            ps.setLong(index, (Long) value);
        } else if (value instanceof String) {
            ps.setString(index, (String) value);
        } else if (value instanceof Integer) {
            ps.setInt(index, (Integer) value);
        } else if (value instanceof BigDecimal) {
            ps.setBigDecimal(index, (BigDecimal) value);
        } else {
            throw new ValidationException("Unsupported parameter type: " + value.getClass() + " at index: " + index);
        }
    }

    private ExchangeRate createFrom(ResultSet rs) throws SQLException {
        Integer id = rs.getInt("id");
        if (rs.wasNull()) {
            id = null;
        }
        Integer baseCurrencyId = rs.getInt("base_currency_id");
        if (rs.wasNull()) {
            baseCurrencyId = null;
        }
        String baseCode = rs.getString("base_code");
        String baseName = rs.getString("base_name");
        String baseSign = rs.getString("base_sign");

        Integer targetCurrencyId = rs.getInt("target_currency_id");
        if (rs.wasNull()) {
            targetCurrencyId = null;
        }
        String targetCode = rs.getString("target_code");
        String targetName = rs.getString("target_name");
        String targetSign = rs.getString("target_sign");
        log.debug("Mapping ExchangeRate from ResultSet: id={}, baseCurrencyId={}, targetCurrencyId={}, rate={}, " +
                        "baseCode='{}', targetCode='{}'", id, baseCurrencyId, targetCurrencyId, rs.getBigDecimal("rate"),
                baseCode, targetCode);
        Currency baseCurrency = new Currency(baseCurrencyId, baseName, baseCode, baseSign);
        Currency targetCurrency = new Currency(targetCurrencyId, targetName, targetCode, targetSign);
        return new ExchangeRate(id, baseCurrency, targetCurrency, rs.getBigDecimal("rate"));
    }

    private boolean isUniqueConstraintViolation(SQLException e) {
        String sqlState = e.getSQLState();
        if (sqlState == null) {
            String message = e.getMessage();
            return message != null && (message.contains("UNIQUE constraint failed") || message.contains("duplicate"));
        }
        return sqlState.startsWith("23");
    }

    private void validateCurrencyIds(Currency baseCurrency, Currency targetCurrency) {
        List<String> missingCurrencies = new ArrayList<>();

        if (baseCurrency.getId() == null) {
            log.warn("Base currency ID is null for code: {}", baseCurrency.getCode());
            missingCurrencies.add(baseCurrency.getCode());
        }

        if (targetCurrency.getId() == null) {
            log.warn("Target currency ID is null for code: {}", targetCurrency.getCode());
            missingCurrencies.add(targetCurrency.getCode());
        }

        if (!missingCurrencies.isEmpty()) {
            throw new NotFoundException(
                    String.format("Currency not found for codes: %s", String.join(", ", missingCurrencies)));
        }
    }
}

package com.jb.currencyexchange.dao;

import com.jb.currencyexchange.db.DataSourceConnectionProvider;
import com.jb.currencyexchange.exception.DatabaseException;
import com.jb.currencyexchange.exception.NotFoundException;
import com.jb.currencyexchange.exception.ValidationException;
import com.jb.currencyexchange.model.Currency;
import com.jb.currencyexchange.validation.structural.CurrencyValidation;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
public class JdbcCurrencyDao implements CurrencyDao {
    private static final String CREATE = "INSERT INTO currency (name, code, sign) VALUES (?, ?, ?)";
    private static final String SELECT_ALL = "SELECT * FROM currency";
    private static final String SELECT_BY_CODE = "SELECT * FROM currency WHERE code=?";
    private static final String UPDATE = "UPDATE currency SET name = ?, code = ?, sign = ? WHERE id = ?";

    @Override
    public Currency create(Currency currency) {
        String normalizedCode = currency.getCode().trim().toUpperCase();
        currency.setCode(normalizedCode);
        CurrencyValidation.validateCurrency(currency.getName(), currency.getCode(), currency.getSign());
        log.debug("Creating currency: id={}, name={}, code={} ", currency.getId(), currency.getName(), currency.getCode());
        try (Connection connection = DataSourceConnectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(CREATE, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, currency.getName());
            ps.setString(2, currency.getCode());
            ps.setString(3, currency.getSign());
            ps.executeUpdate();
            try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    currency.setId(generatedKeys.getInt(1));
                }
            }
            return currency;
        } catch (SQLException e) {
            log.error("Error creating currency: id={}, name={}, code={},: {}", currency.getId(), currency.getName(),
                    currency.getCode(), e.getMessage(), e);
            throw new DatabaseException("Failed to create currency id={}" + currency.getId(), e);
        }
    }

    @Override
    public List<Currency> getAll() {
        try (Connection connection = DataSourceConnectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(SELECT_ALL)) {
            ResultSet rs = ps.executeQuery();
            List<Currency> currencies = new ArrayList<>();
            while (rs.next()) {
                currencies.add(createFrom(rs));
            }
            return currencies;
        } catch (SQLException e) {
            log.error("Error executing query: {}", SELECT_ALL, e);
            throw new DatabaseException("Error executing query: " + SELECT_ALL, e);
        }
    }

    @Override
    public Optional<Currency> getByCode(String code) {
        return executeQuery(SELECT_BY_CODE, code);
    }

    @Override
    public Currency update(Currency currency) {
        CurrencyValidation.validateCurrency(currency.getName(), currency.getCode(), currency.getSign());
        log.debug("Updating currency with ID: {}, code: {}", currency.getId(), currency.getCode());
        if (currency.getId() <= 0) {
            throw new ValidationException("Currency ID must be positive for update");
        }
        try (Connection connection = DataSourceConnectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(UPDATE)) {
            ps.setString(1, currency.getName());
            ps.setString(2, currency.getCode());
            ps.setString(3, currency.getSign());
            ps.setInt(4, currency.getId());
            int rows = ps.executeUpdate();
            if (rows == 0) {
                log.warn("No currency found with ID {} for update, code: {}", currency.getId(), currency.getCode());
                throw new NotFoundException(currency.getCode());
            }
            return currency;
        } catch (SQLException e) {
            log.error("Error updating currency with ID {}: {}", currency.getId(), e.getMessage(), e);
            throw new DatabaseException("Failed to update currency: " + currency.getId(), e);
        }
    }

    private Optional<Currency> executeQuery(String sql, Object paramValue) {
        try (Connection connection = DataSourceConnectionProvider.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            if (paramValue instanceof Integer) {
                ps.setInt(1, (Integer) paramValue);
            } else if (paramValue instanceof String) {
                ps.setString(1, (String) paramValue);
            } else {
                throw new ValidationException("Unsupported type: " + paramValue.getClass());
            }
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return Optional.of(createFrom(rs));
            }
            return Optional.empty();
        } catch (SQLException e) {
            log.error("Error executing query: {}, param: {}", sql, paramValue, e);
            throw new DatabaseException("Error executing query: " + sql, e);
        }
    }

    private Currency createFrom(ResultSet rs) throws SQLException {
        return new Currency(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("code"),
                rs.getString("sign")
        );
    }
}

package mapper;

import com.jb.currencyexchange.dto.response.ExchangeRateResponseDto;
import com.jb.currencyexchange.mapper.ExchangeRateMapper;
import com.jb.currencyexchange.model.Currency;
import com.jb.currencyexchange.model.ExchangeRate;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

public class ExchangeRateMapperTest {
    private final ExchangeRateMapper mapper = ExchangeRateMapper.INSTANCE;

    @Test
    public void testResponseDto() {
        Currency baseCurrency = new Currency(1, "US Dollar", "USD", "$");
        Currency targetCurrency = new Currency(2, "Canadian Dollar", "CAD", "C$");

        ExchangeRate entity = new ExchangeRate(1, baseCurrency, targetCurrency, BigDecimal.valueOf(1.25));

        ExchangeRateResponseDto responseDto = mapper.toResponseDto(entity);

        assertNotNull(responseDto);
        assertNotNull(responseDto.baseCurrency());
        assertNotNull(responseDto.targetCurrency());
        assertEquals("USD", responseDto.baseCurrency().code());
        assertEquals("CAD", responseDto.targetCurrency().code());
        assertEquals(BigDecimal.valueOf(1.25), responseDto.rate());
    }

    @Test
    public void testNullEntityShouldReturnNull() {
        ExchangeRateResponseDto result = mapper.toResponseDto(null);
        assertNull(result);
    }

    @Test
    public void testResponseDtoWithNullRate() {
        Currency baseCurrency = new Currency(1, "US Dollar", "USD", "$");
        Currency targetCurrency = new Currency(2, "Euro", "EUR", "€");
        ExchangeRate entity = new ExchangeRate(1, baseCurrency, targetCurrency, null);

        ExchangeRateResponseDto responseDto = mapper.toResponseDto(entity);

        assertNotNull(responseDto);
        assertNull(responseDto.rate());
        assertEquals("USD", responseDto.baseCurrency().code());
        assertEquals("EUR", responseDto.targetCurrency().code());
    }

    @Test
    public void testResponseDtoWithNullCurrencies() {
        ExchangeRate entity = new ExchangeRate(1, null, null, BigDecimal.valueOf(1.0));

        ExchangeRateResponseDto responseDto = mapper.toResponseDto(entity);

        assertNotNull(responseDto);
        assertNull(responseDto.baseCurrency());
        assertNull(responseDto.targetCurrency());
        assertEquals(BigDecimal.valueOf(1.0), responseDto.rate());
    }
}

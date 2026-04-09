package mapper;

import com.jb.currencyexchange.dto.request.CreateExchangeRateRequestDto;
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
    public void testCreateExchangeRateRequestDto() {
        CreateExchangeRateRequestDto dto = new CreateExchangeRateRequestDto(
                "USD", "EUR", BigDecimal.valueOf(0.85)
        );
        ExchangeRate entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertNotNull(entity.getBaseCode());
        assertNotNull(entity.getTargetCode());

        assertEquals("USD", entity.getBaseCode().getCode());
        assertEquals("EUR", entity.getTargetCode().getCode());
        assertEquals(BigDecimal.valueOf(0.85), entity.getRate());
        assertNull(entity.getId());
    }

    @Test
    public void testResponseDto() {
        Currency baseCurrency = new Currency(1, "US Dollar", "USD", "$");
        Currency targetCurrency = new Currency(2, "Canadian Dollar", "CAD", "C$");

        ExchangeRate entity = ExchangeRate.builder()
                .id(1)
                .baseCode(baseCurrency)
                .targetCode(targetCurrency)
                .rate(BigDecimal.valueOf(1.25))
                .build();

        ExchangeRateResponseDto responseDto = mapper.toResponseDto(entity);

        assertNotNull(responseDto);
        assertNotNull(responseDto.baseCurrency());
        assertNotNull(responseDto.targetCurrency());
        assertEquals("USD", responseDto.baseCurrency().code());
        assertEquals("CAD", responseDto.targetCurrency().code());
        assertEquals(BigDecimal.valueOf(1.25), responseDto.rate());
    }

    @Test
    public void testNullDtoShouldReturnNull() {
        ExchangeRate result = mapper.toEntity((CreateExchangeRateRequestDto) null);
        assertNull(result);
    }

    @Test
    public void testNullEntityShouldReturnNull() {
        ExchangeRateResponseDto result = mapper.toResponseDto(null);
        assertNull(result);
    }

    @Test
    public void testWithEmptyFields() {
        CreateExchangeRateRequestDto dto = new CreateExchangeRateRequestDto("", "", BigDecimal.ZERO);
        ExchangeRate entity = mapper.toEntity(dto);

        assertNotNull(entity);
        assertNotNull(entity.getBaseCode());
        assertNotNull(entity.getTargetCode());

        assertEquals("", entity.getBaseCode().getCode());
        assertEquals("", entity.getTargetCode().getCode());
        assertEquals(BigDecimal.ZERO, entity.getRate());
    }

    @Test
    public void testCurrencyCodesAreTrimmedAndUppercased() {
        CreateExchangeRateRequestDto dto = new CreateExchangeRateRequestDto(
                " usd ", " eur ", BigDecimal.valueOf(0.85)
        );
        ExchangeRate entity = mapper.toEntity(dto);

        assertEquals("USD", entity.getBaseCode().getCode());
        assertEquals("EUR", entity.getTargetCode().getCode());
    }

    @Test
    public void testResponseDtoWithNullCurrencies() {
        ExchangeRate entity = ExchangeRate.builder()
                .id(1)
                .baseCode(null)
                .targetCode(null)
                .rate(BigDecimal.valueOf(1.0))
                .build();

        ExchangeRateResponseDto responseDto = mapper.toResponseDto(entity);

        assertNotNull(responseDto);
        assertNull(responseDto.baseCurrency());
        assertNull(responseDto.targetCurrency());
        assertEquals(BigDecimal.valueOf(1.0), responseDto.rate());
    }
}

package mapper;

import com.jb.currencyexchange.dto.request.CreateCurrencyRequestDto;
import com.jb.currencyexchange.dto.response.CurrencyResponseDto;
import com.jb.currencyexchange.mapper.CurrencyMapper;
import com.jb.currencyexchange.model.Currency;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class CurrencyMapperTest {
    private final CurrencyMapper mapper = CurrencyMapper.INSTANCE;

    @Test
    public void testCreateCurrencyRequestDto() {
        CreateCurrencyRequestDto dto = new CreateCurrencyRequestDto("US Dollar", "USD", "$");
        Currency entity = mapper.toEntity(dto);
        assertNotNull(entity);
        assertEquals("USD", entity.getCode());
        assertEquals("US Dollar", entity.getName());
        assertEquals("$", entity.getSign());
        assertNull(entity.getId());
    }

    @Test
    public void testToResponseDto() {
        Currency entity = Currency.builder()
                .id(1)
                .name("British Pound")
                .code("GBP")
                .sign("£")
                .build();
        CurrencyResponseDto responseDto = mapper.toResponseDto(entity);
        assertNotNull(responseDto);
        assertEquals("GBP", responseDto.code());
        assertEquals("British Pound", responseDto.name());
        assertEquals("£", responseDto.sign());
    }

    @Test
    public void testNullDtoShouldReturnNull() {
        Currency result = mapper.toEntity((CreateCurrencyRequestDto) null);
        assertNull(result);
    }

    @Test
    public void testNullEntityShouldReturnNull() {
        CurrencyResponseDto result = mapper.toResponseDto(null);
        assertNull(result);
    }

    @Test
    public void testToEntity_WithEmptyFields() {
        CreateCurrencyRequestDto dto = new CreateCurrencyRequestDto("", "", "");
        Currency entity = mapper.toEntity(dto);
        assertEquals("", entity.getCode());
        assertEquals("", entity.getName());
        assertEquals("", entity.getSign());
    }
}

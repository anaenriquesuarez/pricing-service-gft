package com.inditex.pricing.application.service;

import com.inditex.pricing.domain.exception.PriceNotFoundException;
import com.inditex.pricing.domain.model.Price;
import com.inditex.pricing.domain.port.PriceRepositoryPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/** Isolated unit tests: no Spring context and no database. */
@ExtendWith(MockitoExtension.class)
class GetApplicablePriceServiceTest {

    @Mock
    private PriceRepositoryPort repositoryPort;

    @InjectMocks
    private GetApplicablePriceService service;

    private final LocalDateTime applicationDate = LocalDateTime.of(2020, 6, 14, 16, 0);

    @Test
    void returnsPriceWhenRepositoryFindsApplicableTariff() {
        Price price = new Price(2L, 1L, 35455L, 2L,
                LocalDateTime.of(2020, 6, 14, 15, 0),
                LocalDateTime.of(2020, 6, 14, 18, 30),
                1, new BigDecimal("25.45"), "EUR");
        when(repositoryPort.findApplicablePrice(applicationDate, 35455L, 1L))
                .thenReturn(Optional.of(price));

        Price result = service.execute(applicationDate, 35455L, 1L);

        assertSame(price, result);
        assertEquals(0, new BigDecimal("25.45").compareTo(result.amount()));
        verify(repositoryPort).findApplicablePrice(applicationDate, 35455L, 1L);
        verifyNoMoreInteractions(repositoryPort);
    }

    @Test
    void throwsDomainExceptionWhenNoTariffApplies() {
        when(repositoryPort.findApplicablePrice(applicationDate, 35455L, 1L))
                .thenReturn(Optional.empty());

        PriceNotFoundException exception = assertThrows(PriceNotFoundException.class,
                () -> service.execute(applicationDate, 35455L, 1L));

        assertTrue(exception.getMessage().contains("35455"));
        verify(repositoryPort).findApplicablePrice(applicationDate, 35455L, 1L);
        verifyNoMoreInteractions(repositoryPort);
    }
}

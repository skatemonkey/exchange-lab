package dev.exchangelab.finance.redis;

import dev.exchangelab.finance.persistence.StockPositionEntity;
import dev.exchangelab.finance.persistence.StockPositionRepository;
import dev.exchangelab.finance.persistence.TraderAccountEntity;
import dev.exchangelab.finance.persistence.TraderAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RedisAvailabilityInitializerTest {

    @Mock
    private TraderAccountRepository traderAccountRepository;

    @Mock
    private StockPositionRepository stockPositionRepository;

    @Mock
    private RedisReservationService redisReservationService;

    @InjectMocks
    private RedisAvailabilityInitializer initializer;

    @Test
    void preloadsAvailableCashAndStockFromDatabase() {
        UUID traderId = UUID.randomUUID();
        TraderAccountEntity account = new TraderAccountEntity(
                traderId,
                amount("1000"),
                amount("200")
        );
        StockPositionEntity position = new StockPositionEntity(
                UUID.randomUUID(),
                traderId,
                "ACME",
                amount("50"),
                amount("10")
        );
        when(traderAccountRepository.findAll()).thenReturn(List.of(account));
        when(stockPositionRepository.findAll()).thenReturn(List.of(position));

        initializer.run(null);

        verify(redisReservationService).setAvailableCash(traderId, amount("800"));
        verify(redisReservationService).setAvailableStock(traderId, "ACME", amount("40"));
    }

    private static BigDecimal amount(String value) {
        return new BigDecimal(value);
    }
}

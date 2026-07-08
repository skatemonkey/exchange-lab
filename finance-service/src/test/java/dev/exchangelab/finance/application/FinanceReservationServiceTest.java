package dev.exchangelab.finance.application;

import dev.exchangelab.common.finance.ReserveCashRequest;
import dev.exchangelab.common.finance.ReserveStockRequest;
import dev.exchangelab.finance.persistence.StockPositionEntity;
import dev.exchangelab.finance.persistence.StockPositionRepository;
import dev.exchangelab.finance.persistence.TraderAccountEntity;
import dev.exchangelab.finance.persistence.TraderAccountRepository;
import dev.exchangelab.finance.redis.RedisReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceReservationServiceTest {

    private static final String SYMBOL = "ACME";

    @Mock
    private RedisReservationService redisReservationService;

    @Mock
    private TraderAccountRepository traderAccountRepository;

    @Mock
    private StockPositionRepository stockPositionRepository;

    @InjectMocks
    private FinanceReservationService financeReservationService;

    @Test
    void reservesCashUsingLoadedRedisBalance() {
        UUID traderId = UUID.randomUUID();
        TraderAccountEntity account = new TraderAccountEntity(
                traderId,
                money("1000"),
                money("0")
        );
        when(traderAccountRepository.findByIdForUpdate(traderId))
                .thenReturn(Optional.of(account));
        when(redisReservationService.reserveCashIfLoaded(traderId, money("1000")))
                .thenReturn(true);

        var response = financeReservationService.reserveCash(new ReserveCashRequest(
                traderId,
                money("1000")
        ));

        assertThat(response.reservedCash()).isEqualByComparingTo("1000");
        assertThat(account.getReservedCash()).isEqualByComparingTo("1000");
        verify(traderAccountRepository).save(account);
    }

    @Test
    void initializesCashFromDatabaseWhenRedisBalanceIsMissing() {
        UUID traderId = UUID.randomUUID();
        when(redisReservationService.reserveCashIfLoaded(traderId, money("100")))
                .thenReturn(false);
        TraderAccountEntity account = new TraderAccountEntity(
                traderId,
                money("1000"),
                money("200")
        );
        when(traderAccountRepository.findByIdForUpdate(traderId))
                .thenReturn(Optional.of(account));

        financeReservationService.reserveCash(new ReserveCashRequest(traderId, money("100")));

        verify(redisReservationService).reserveCash(traderId, money("100"), money("800"));
        assertThat(account.getReservedCash()).isEqualByComparingTo("300");
        verify(traderAccountRepository).save(account);
    }

    @Test
    void initializesStockFromDatabaseWhenRedisBalanceIsMissing() {
        UUID traderId = UUID.randomUUID();
        when(redisReservationService.reserveStockIfLoaded(traderId, SYMBOL, quantity("4")))
                .thenReturn(false);
        StockPositionEntity position = new StockPositionEntity(
                UUID.randomUUID(),
                traderId,
                SYMBOL,
                quantity("10"),
                quantity("3")
        );
        when(stockPositionRepository.findByTraderIdAndSymbolForUpdate(traderId, SYMBOL))
                .thenReturn(Optional.of(position));

        financeReservationService.reserveStock(new ReserveStockRequest(
                traderId,
                SYMBOL,
                quantity("4")
        ));

        verify(redisReservationService).reserveStock(traderId, SYMBOL, quantity("4"), quantity("7"));
        assertThat(position.getReservedQuantity()).isEqualByComparingTo("7");
        verify(stockPositionRepository).save(position);
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private static BigDecimal quantity(String value) {
        return new BigDecimal(value);
    }
}

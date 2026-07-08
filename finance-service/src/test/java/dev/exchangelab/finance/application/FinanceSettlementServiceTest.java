package dev.exchangelab.finance.application;

import dev.exchangelab.common.event.TradeMatchedEvent;
import dev.exchangelab.finance.metrics.FinanceMetrics;
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
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FinanceSettlementServiceTest {

    private static final String SYMBOL = "ACME";

    @Mock
    private TraderAccountRepository traderAccountRepository;

    @Mock
    private StockPositionRepository stockPositionRepository;

    @Mock
    private RedisReservationService redisReservationService;

    @Mock
    private FinanceMetrics financeMetrics;

    @InjectMocks
    private FinanceSettlementService financeSettlementService;

    @Test
    void settlesTradeInDatabaseAndRedis() {
        UUID buyerId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        TraderAccountEntity buyerAccount = new TraderAccountEntity(
                buyerId,
                money("1000"),
                money("1000")
        );
        TraderAccountEntity sellerAccount = new TraderAccountEntity(
                sellerId,
                money("0"),
                money("0")
        );
        StockPositionEntity sellerPosition = new StockPositionEntity(
                UUID.randomUUID(),
                sellerId,
                SYMBOL,
                quantity("10"),
                quantity("10")
        );

        when(traderAccountRepository.findByIdForUpdate(buyerId))
                .thenReturn(Optional.of(buyerAccount));
        when(traderAccountRepository.findByIdForUpdate(sellerId))
                .thenReturn(Optional.of(sellerAccount));
        when(stockPositionRepository.findByTraderIdAndSymbolForUpdate(sellerId, SYMBOL))
                .thenReturn(Optional.of(sellerPosition));
        when(stockPositionRepository.findByTraderIdAndSymbolForUpdate(buyerId, SYMBOL))
                .thenReturn(Optional.empty());

        financeSettlementService.settle(new TradeMatchedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                buyerId,
                sellerId,
                SYMBOL,
                money("90"),
                quantity("10"),
                money("100"),
                Instant.parse("2026-01-01T00:00:00Z")
        ));

        assertThat(buyerAccount.getCashBalance()).isEqualByComparingTo("100");
        assertThat(buyerAccount.getReservedCash()).isEqualByComparingTo("0");
        assertThat(sellerAccount.getCashBalance()).isEqualByComparingTo("900");
        assertThat(sellerPosition.getQuantity()).isEqualByComparingTo("0");
        assertThat(sellerPosition.getReservedQuantity()).isEqualByComparingTo("0");

        verify(traderAccountRepository).saveAll(any());
        verify(stockPositionRepository).saveAll(any());
        verify(redisReservationService).increaseAvailableCash(sellerId, money("900"), money("0"));
        verify(redisReservationService).increaseAvailableStock(buyerId, SYMBOL, quantity("10"), quantity("0"));
        verify(redisReservationService).increaseAvailableCash(buyerId, money("100"), money("0"));
        verify(financeMetrics).settlementCompleted();
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private static BigDecimal quantity(String value) {
        return new BigDecimal(value);
    }
}

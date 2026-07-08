package dev.exchangelab.application;

import dev.exchangelab.common.order.OrderSide;

import dev.exchangelab.common.event.LimitOrderSubmittedEvent;
import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.StockPosition;
import dev.exchangelab.domain.model.TraderAccount;
import dev.exchangelab.domain.repository.StockPositionRepository;
import dev.exchangelab.domain.repository.TraderAccountRepository;
import dev.exchangelab.infrastructure.redis.RedisReservationService;
import dev.exchangelab.common.dto.PlaceLimitOrderRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceLimitOrderUseCaseImplTest {

    private static final String SYMBOL = "ACME";

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @Mock
    private RedisReservationService redisReservationService;

    @Mock
    private TraderAccountRepository traderAccountRepository;

    @Mock
    private StockPositionRepository stockPositionRepository;

    @InjectMocks
    private PlaceLimitOrderUseCaseImpl placeLimitOrderUseCase;

    @Test
    void reservesCashInRedisBeforePublishingBuyOrderEvent() {
        UUID traderId = UUID.randomUUID();
        when(redisReservationService.reserveCashIfLoaded(traderId, money("1000")))
                .thenReturn(true);

        placeLimitOrderUseCase.placeLimitOrder(new PlaceLimitOrderRequest(
                traderId,
                SYMBOL,
                OrderSide.BUY,
                money("100"),
                quantity("10")
        ));

        ArgumentCaptor<LimitOrderSubmittedEvent> eventCaptor =
                ArgumentCaptor.forClass(LimitOrderSubmittedEvent.class);
        InOrder inOrder = inOrder(redisReservationService, orderEventPublisher);
        inOrder.verify(redisReservationService).reserveCashIfLoaded(traderId, money("1000"));
        inOrder.verify(orderEventPublisher).publish(eventCaptor.capture());

        LimitOrderSubmittedEvent event = eventCaptor.getValue();
        assertThat(event.reservedCash()).isEqualByComparingTo("1000");
        assertThat(event.reservedStock()).isNull();
        verify(traderAccountRepository, never()).findForCashReservation(any());
    }

    @Test
    void initializesAvailableCashWhenRedisCashIsMissing() {
        UUID traderId = UUID.randomUUID();
        when(redisReservationService.reserveCashIfLoaded(traderId, money("100")))
                .thenReturn(false);
        when(traderAccountRepository.findForCashReservation(traderId))
                .thenReturn(Optional.of(new TraderAccount(
                        traderId,
                        money("1000"),
                        money("200")
                )));

        placeLimitOrderUseCase.placeLimitOrder(new PlaceLimitOrderRequest(
                traderId,
                SYMBOL,
                OrderSide.BUY,
                money("100"),
                quantity("1")
        ));

        InOrder inOrder = inOrder(redisReservationService, orderEventPublisher);
        inOrder.verify(redisReservationService).reserveCashIfLoaded(traderId, money("100"));
        inOrder.verify(redisReservationService).reserveCash(traderId, money("100"), money("800"));
        inOrder.verify(orderEventPublisher).publish(any());
    }

    @Test
    void reservesStockInRedisBeforePublishingSellOrderEvent() {
        UUID traderId = UUID.randomUUID();
        when(redisReservationService.reserveStockIfLoaded(traderId, SYMBOL, quantity("4")))
                .thenReturn(true);

        placeLimitOrderUseCase.placeLimitOrder(new PlaceLimitOrderRequest(
                traderId,
                SYMBOL,
                OrderSide.SELL,
                money("100"),
                quantity("4")
        ));

        ArgumentCaptor<LimitOrderSubmittedEvent> eventCaptor =
                ArgumentCaptor.forClass(LimitOrderSubmittedEvent.class);
        InOrder inOrder = inOrder(redisReservationService, orderEventPublisher);
        inOrder.verify(redisReservationService).reserveStockIfLoaded(traderId, SYMBOL, quantity("4"));
        inOrder.verify(orderEventPublisher).publish(eventCaptor.capture());

        LimitOrderSubmittedEvent event = eventCaptor.getValue();
        assertThat(event.reservedCash()).isNull();
        assertThat(event.reservedStock()).isEqualByComparingTo("4");
        verify(stockPositionRepository, never()).findForStockReservation(any(), any());
    }

    @Test
    void initializesAvailableStockWhenRedisStockIsMissing() {
        UUID traderId = UUID.randomUUID();
        when(redisReservationService.reserveStockIfLoaded(traderId, SYMBOL, quantity("2")))
                .thenReturn(false);
        when(stockPositionRepository.findForStockReservation(traderId, SYMBOL))
                .thenReturn(Optional.of(new StockPosition(
                        UUID.randomUUID(),
                        traderId,
                        SYMBOL,
                        quantity("10"),
                        quantity("3")
                )));

        placeLimitOrderUseCase.placeLimitOrder(new PlaceLimitOrderRequest(
                traderId,
                SYMBOL,
                OrderSide.SELL,
                money("100"),
                quantity("2")
        ));

        InOrder inOrder = inOrder(redisReservationService, orderEventPublisher);
        inOrder.verify(redisReservationService).reserveStockIfLoaded(traderId, SYMBOL, quantity("2"));
        inOrder.verify(redisReservationService).reserveStock(traderId, SYMBOL, quantity("2"), quantity("7"));
        inOrder.verify(orderEventPublisher).publish(any());
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private static BigDecimal quantity(String value) {
        return new BigDecimal(value);
    }
}

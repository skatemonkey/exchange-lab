package dev.exchangelab.application;

import dev.exchangelab.common.order.OrderSide;

import dev.exchangelab.common.event.LimitOrderSubmittedEvent;
import dev.exchangelab.application.orderbook.InMemoryOrderBookRegistry;
import dev.exchangelab.domain.model.MatchResult;
import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.StockPosition;
import dev.exchangelab.domain.model.Trade;
import dev.exchangelab.domain.model.TraderAccount;
import dev.exchangelab.domain.repository.OrderRepository;
import dev.exchangelab.domain.repository.StockPositionRepository;
import dev.exchangelab.domain.repository.TradeRepository;
import dev.exchangelab.domain.repository.TraderAccountRepository;
import dev.exchangelab.infrastructure.redis.RedisReservationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProcessLimitOrderUseCaseImplTest {

    private static final String SYMBOL = "ACME";

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TradeRepository tradeRepository;

    @Mock
    private TraderAccountRepository traderAccountRepository;

    @Mock
    private StockPositionRepository stockPositionRepository;

    @Mock
    private InMemoryOrderBookRegistry inMemoryOrderBookRegistry;

    @Mock
    private RedisReservationService redisReservationService;

    @InjectMocks
    private ProcessLimitOrderUseCaseImpl processLimitOrderUseCase;

    @Test
    void syncsMysqlReservationAndRedisAvailabilityAfterTrade() {
        UUID buyerId = UUID.randomUUID();
        UUID sellerId = UUID.randomUUID();
        Order restingSellOrder = Order.createLimit(
                UUID.randomUUID(),
                sellerId,
                SYMBOL,
                OrderSide.SELL,
                money("90"),
                quantity("10"),
                Instant.parse("2026-01-01T00:00:00Z")
        );
        TraderAccount buyerAccount = new TraderAccount(buyerId, money("1000"), money("0"));
        TraderAccount sellerAccount = new TraderAccount(sellerId, money("0"), money("0"));
        StockPosition sellerPosition = new StockPosition(
                UUID.randomUUID(),
                sellerId,
                SYMBOL,
                quantity("10"),
                quantity("10")
        );

        when(traderAccountRepository.findForCashReservation(buyerId))
                .thenReturn(Optional.of(buyerAccount));
        when(traderAccountRepository.findForCashReservation(sellerId))
                .thenReturn(Optional.of(sellerAccount));
        when(stockPositionRepository.findForStockReservation(sellerId, SYMBOL))
                .thenReturn(Optional.of(sellerPosition));
        when(stockPositionRepository.findForStockReservation(buyerId, SYMBOL))
                .thenReturn(Optional.empty());
        when(inMemoryOrderBookRegistry.match(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order incomingOrder = invocation.getArgument(0);
                    Trade trade = Trade.create(incomingOrder, restingSellOrder, quantity("10"));
                    incomingOrder.fill(quantity("10"));
                    restingSellOrder.fill(quantity("10"));
                    return new MatchResult(incomingOrder, List.of(restingSellOrder), List.of(trade));
                });

        processLimitOrderUseCase.process(new LimitOrderSubmittedEvent(
                UUID.randomUUID(),
                buyerId,
                SYMBOL,
                OrderSide.BUY,
                money("100"),
                quantity("10"),
                money("1000"),
                null,
                Instant.parse("2026-01-01T00:01:00Z")
        ));

        verify(redisReservationService).increaseAvailableCash(buyerId, money("100"), money("0"));
        verify(redisReservationService).increaseAvailableCash(sellerId, money("900"), money("0"));
        verify(redisReservationService).increaseAvailableStock(buyerId, SYMBOL, quantity("10"), quantity("0"));
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private static BigDecimal quantity(String value) {
        return new BigDecimal(value);
    }
}

package dev.exchangelab.match.application;

import dev.exchangelab.common.order.OrderSide;

import dev.exchangelab.common.event.LimitOrderSubmittedEvent;
import dev.exchangelab.match.application.orderbook.InMemoryOrderBookRegistry;
import dev.exchangelab.match.domain.model.MatchResult;
import dev.exchangelab.match.domain.model.Order;
import dev.exchangelab.match.domain.model.Trade;
import dev.exchangelab.match.domain.repository.OrderRepository;
import dev.exchangelab.match.domain.repository.TradeRepository;
import dev.exchangelab.match.kafka.TradeEventPublisher;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
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
    private InMemoryOrderBookRegistry inMemoryOrderBookRegistry;

    @Mock
    private TradeEventPublisher tradeEventPublisher;

    @InjectMocks
    private ProcessLimitOrderUseCaseImpl processLimitOrderUseCase;

    @Test
    void savesMatchedOrdersAndTrades() {
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
        UUID incomingOrderId = UUID.randomUUID();

        when(inMemoryOrderBookRegistry.match(any(Order.class)))
                .thenAnswer(invocation -> {
                    Order incomingOrder = invocation.getArgument(0);
                    Trade trade = Trade.create(incomingOrder, restingSellOrder, quantity("10"));
                    incomingOrder.fill(quantity("10"));
                    restingSellOrder.fill(quantity("10"));
                    return new MatchResult(incomingOrder, List.of(restingSellOrder), List.of(trade));
                });

        processLimitOrderUseCase.process(new LimitOrderSubmittedEvent(
                incomingOrderId,
                buyerId,
                SYMBOL,
                OrderSide.BUY,
                money("100"),
                quantity("10"),
                money("1000"),
                null,
                Instant.parse("2026-01-01T00:01:00Z")
        ));

        verify(orderRepository).saveAll(argThat(orders ->
                orders.size() == 2
                        && orders.stream().anyMatch(order -> order.getOrderId().equals(incomingOrderId))
                        && orders.stream().anyMatch(order -> order.getOrderId().equals(restingSellOrder.getOrderId()))
        ));
        verify(tradeRepository).saveAll(argThat(trades ->
                trades.size() == 1
                        && trades.getFirst().getBuyOrderId().equals(incomingOrderId)
                        && trades.getFirst().getSellOrderId().equals(restingSellOrder.getOrderId())
        ));
        verify(tradeEventPublisher).publish(argThat(event ->
                event.buyOrderId().equals(incomingOrderId)
                        && event.sellOrderId().equals(restingSellOrder.getOrderId())
                        && event.buyerTraderId().equals(buyerId)
                        && event.sellerTraderId().equals(sellerId)
                        && event.price().compareTo(money("90")) == 0
                        && event.quantity().compareTo(quantity("10")) == 0
                        && event.buyOrderLimitPrice().compareTo(money("100")) == 0
        ));
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private static BigDecimal quantity(String value) {
        return new BigDecimal(value);
    }
}

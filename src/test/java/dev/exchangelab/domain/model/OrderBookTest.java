package dev.exchangelab.domain.model;

import dev.exchangelab.common.order.OrderSide;
import dev.exchangelab.common.order.OrderStatus;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OrderBookTest {

    private static final String SYMBOL = "ACME";

    @Test
    void matchesLowestSellPriceFirst() {
        OrderBook orderBook = new OrderBook(SYMBOL);
        Order expensiveSell = order(OrderSide.SELL, "105", "10", "2026-01-01T00:00:00Z");
        Order cheapSell = order(OrderSide.SELL, "100", "10", "2026-01-01T00:01:00Z");
        orderBook.add(expensiveSell);
        orderBook.add(cheapSell);

        MatchResult result = orderBook.match(order(OrderSide.BUY, "110", "10", "2026-01-01T00:02:00Z"));

        assertThat(result.trades()).hasSize(1);
        assertThat(result.trades().getFirst().getSellOrderId()).isEqualTo(cheapSell.getOrderId());
        assertThat(cheapSell.getStatus()).isEqualTo(OrderStatus.FILLED);
        assertThat(expensiveSell.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }

    @Test
    void matchesSamePriceByFifo() {
        OrderBook orderBook = new OrderBook(SYMBOL);
        Order olderSell = order(OrderSide.SELL, "100", "10", "2026-01-01T00:00:00Z");
        Order newerSell = order(OrderSide.SELL, "100", "10", "2026-01-01T00:01:00Z");
        orderBook.add(olderSell);
        orderBook.add(newerSell);

        MatchResult result = orderBook.match(order(OrderSide.BUY, "100", "10", "2026-01-01T00:02:00Z"));

        assertThat(result.trades()).hasSize(1);
        assertThat(result.trades().getFirst().getSellOrderId()).isEqualTo(olderSell.getOrderId());
        assertThat(olderSell.getStatus()).isEqualTo(OrderStatus.FILLED);
        assertThat(newerSell.getStatus()).isEqualTo(OrderStatus.ACCEPTED);
    }

    @Test
    void keepsUnfilledIncomingOrderInBook() {
        OrderBook orderBook = new OrderBook(SYMBOL);
        Order buyOrder = order(OrderSide.BUY, "100", "10", "2026-01-01T00:00:00Z");
        orderBook.match(buyOrder);

        MatchResult result = orderBook.match(order(OrderSide.SELL, "90", "4", "2026-01-01T00:01:00Z"));

        assertThat(result.trades()).hasSize(1);
        assertThat(result.trades().getFirst().getBuyOrderId()).isEqualTo(buyOrder.getOrderId());
        assertThat(result.trades().getFirst().getQuantity()).isEqualByComparingTo("4");
        assertThat(buyOrder.getStatus()).isEqualTo(OrderStatus.PARTIALLY_FILLED);
        assertThat(buyOrder.getRemainingQuantity()).isEqualByComparingTo("6");
    }

    private static Order order(OrderSide side, String price, String quantity, String createdAt) {
        return Order.createLimit(
                UUID.randomUUID(),
                UUID.randomUUID(),
                SYMBOL,
                side,
                new BigDecimal(price),
                new BigDecimal(quantity),
                Instant.parse(createdAt)
        );
    }
}

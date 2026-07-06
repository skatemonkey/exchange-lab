package dev.exchangelab.application.orderbook;

import dev.exchangelab.domain.model.MatchResult;
import dev.exchangelab.domain.model.Order;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryOrderBookRegistryTest {

    private static final String SYMBOL = "ACME";

    @Test
    void rebuildsOpenOrdersByCreatedAt() {
        InMemoryOrderBookRegistry registry = new InMemoryOrderBookRegistry();
        Order olderSell = order(Order.Side.SELL, "100", "10", "2026-01-01T00:00:00Z");
        Order newerSell = order(Order.Side.SELL, "100", "10", "2026-01-01T00:01:00Z");

        registry.rebuild(List.of(newerSell, olderSell));
        MatchResult result = registry.match(order(Order.Side.BUY, "100", "10", "2026-01-01T00:02:00Z"));

        assertThat(result.trades()).hasSize(1);
        assertThat(result.trades().getFirst().getSellOrderId()).isEqualTo(olderSell.getOrderId());
    }

    private static Order order(Order.Side side, String price, String quantity, String createdAt) {
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

package dev.exchangelab.match.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class MatchMetrics {

    private final Counter processedOrders;
    private final Counter createdTrades;

    public MatchMetrics(MeterRegistry meterRegistry) {
        processedOrders = Counter.builder("match.orders.processed")
                .description("Orders processed by match-service")
                .register(meterRegistry);
        createdTrades = Counter.builder("match.trades.created")
                .description("Trades created by match-service")
                .register(meterRegistry);
    }

    public void orderProcessed() {
        processedOrders.increment();
    }

    public void tradesCreated(int count) {
        createdTrades.increment(count);
    }
}

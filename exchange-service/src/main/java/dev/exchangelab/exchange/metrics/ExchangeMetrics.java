package dev.exchangelab.exchange.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class ExchangeMetrics {

    private final Counter acceptedOrders;

    public ExchangeMetrics(MeterRegistry meterRegistry) {
        acceptedOrders = Counter.builder("exchange.orders.accepted")
                .description("Orders accepted by exchange-service")
                .register(meterRegistry);
    }

    public void orderAccepted() {
        acceptedOrders.increment();
    }
}

package dev.exchangelab.finance.metrics;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class FinanceMetrics {

    private final Counter completedReservations;
    private final Counter completedSettlements;

    public FinanceMetrics(MeterRegistry meterRegistry) {
        completedReservations = Counter.builder("finance.reservations.completed")
                .description("Cash and stock reservations completed by finance-service")
                .register(meterRegistry);
        completedSettlements = Counter.builder("finance.settlements.completed")
                .description("Trade settlements completed by finance-service")
                .register(meterRegistry);
    }

    public void reservationCompleted() {
        completedReservations.increment();
    }

    public void settlementCompleted() {
        completedSettlements.increment();
    }
}

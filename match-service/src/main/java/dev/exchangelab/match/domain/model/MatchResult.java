package dev.exchangelab.match.domain.model;

import java.util.List;

public record MatchResult(
        Order incomingOrder,
        List<Order> updatedRestingOrders,
        List<Trade> trades
) {

    public MatchResult {
        updatedRestingOrders = List.copyOf(updatedRestingOrders);
        trades = List.copyOf(trades);
    }
}

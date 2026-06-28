package dev.exchangelab.domain.model;

import java.util.List;

public record MatchResult(
        Order incomingOrder,
        List<Order> updatedMatchingOrders,
        List<Trade> executedTrades
) {
}

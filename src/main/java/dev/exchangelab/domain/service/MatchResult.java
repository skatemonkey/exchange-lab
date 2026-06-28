package dev.exchangelab.domain.service;

import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.Trade;

import java.util.List;

public record MatchResult(
        Order incomingOrder,
        List<Order> updatedMatchingOrders,
        List<Trade> executedTrades
) {
}

package dev.exchangelab.domain.model;

import java.util.ArrayList;
import java.util.List;

public record MatchResult(
        Order incomingOrder,
        List<Order> updatedMatchingOrders,
        List<Trade> executedTrades
) {

    public List<Order> ordersToSave() {
        List<Order> orders = new ArrayList<>();
        orders.add(incomingOrder);
        orders.addAll(updatedMatchingOrders);
        return orders;
    }
}

package dev.exchangelab.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OrderBook {

    private final List<Order> restingOrders;

    public OrderBook(List<Order> restingOrders) {
        this.restingOrders = restingOrders;
    }

    public MatchResult match(Order incomingOrder) {
        List<Trade> executedTrades = new ArrayList<>();
        List<Order> updatedRestingOrders = new ArrayList<>();

        for (Order restingOrder : restingOrders) {
            if (incomingOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal tradeQuantity = incomingOrder.getRemainingQuantity().min(
                    restingOrder.getRemainingQuantity()
            );

            executedTrades.add(createTrade(incomingOrder, restingOrder, tradeQuantity));
            updatedRestingOrders.add(restingOrder);

            incomingOrder.fill(tradeQuantity);
            restingOrder.fill(tradeQuantity);
        }

        return new MatchResult(incomingOrder, updatedRestingOrders, executedTrades);
    }

    private Trade createTrade(
            Order incomingOrder,
            Order restingOrder,
            BigDecimal tradeQuantity
    ) {
        Order buyOrder = incomingOrder.getSide() == Order.Side.BUY ? incomingOrder : restingOrder;
        Order sellOrder = incomingOrder.getSide() == Order.Side.SELL ? incomingOrder : restingOrder;

        return new Trade(
                UUID.randomUUID(),
                buyOrder.getOrderId(),
                sellOrder.getOrderId(),
                buyOrder.getTraderId(),
                sellOrder.getTraderId(),
                incomingOrder.getSymbol(),
                restingOrder.getLimitPrice(),
                tradeQuantity,
                Instant.now()
        );
    }
}

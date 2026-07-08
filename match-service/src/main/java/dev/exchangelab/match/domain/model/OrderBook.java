package dev.exchangelab.match.domain.model;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.NavigableMap;
import java.util.TreeMap;

public class OrderBook {

    private final String symbol;
    private final NavigableMap<BigDecimal, Deque<Order>> buyOrders =
            new TreeMap<>(Comparator.reverseOrder());
    private final NavigableMap<BigDecimal, Deque<Order>> sellOrders = new TreeMap<>();

    public OrderBook(String symbol) {
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Order book symbol is required");
        }

        this.symbol = symbol;
    }

    public synchronized MatchResult match(Order incomingOrder) {
        validateOrder(incomingOrder);

        List<Order> updatedRestingOrders = new ArrayList<>();
        List<Trade> trades = new ArrayList<>();
        NavigableMap<BigDecimal, Deque<Order>> oppositeSide = oppositeSideFor(incomingOrder);

        while (incomingOrder.isOpen() && !oppositeSide.isEmpty()) {
            var bestPriceLevel = oppositeSide.firstEntry();
            if (!canMatch(incomingOrder, bestPriceLevel.getKey())) {
                break;
            }

            Deque<Order> restingOrders = bestPriceLevel.getValue();
            matchPriceLevel(incomingOrder, restingOrders, updatedRestingOrders, trades);

            if (restingOrders.isEmpty()) {
                oppositeSide.pollFirstEntry();
            }
        }

        if (incomingOrder.isOpen()) {
            add(incomingOrder);
        }

        return new MatchResult(incomingOrder, updatedRestingOrders, trades);
    }

    public synchronized void add(Order order) {
        validateOrder(order);

        if (!order.isOpen()) {
            return;
        }

        sideFor(order)
                .computeIfAbsent(order.getLimitPrice(), ignored -> new ArrayDeque<>())
                .addLast(order);
    }

    private void matchPriceLevel(
            Order incomingOrder,
            Deque<Order> restingOrders,
            List<Order> updatedRestingOrders,
            List<Trade> trades
    ) {
        while (incomingOrder.isOpen() && !restingOrders.isEmpty()) {
            Order restingOrder = restingOrders.peekFirst();
            if (!restingOrder.isOpen()) {
                restingOrders.removeFirst();
                continue;
            }

            BigDecimal tradeQuantity = incomingOrder.getRemainingQuantity()
                    .min(restingOrder.getRemainingQuantity());

            trades.add(Trade.create(incomingOrder, restingOrder, tradeQuantity));
            incomingOrder.fill(tradeQuantity);
            restingOrder.fill(tradeQuantity);
            updatedRestingOrders.add(restingOrder);

            if (!restingOrder.isOpen()) {
                restingOrders.removeFirst();
            }
        }
    }

    private boolean canMatch(Order incomingOrder, BigDecimal restingPrice) {
        return switch (incomingOrder.getSide()) {
            case BUY -> restingPrice.compareTo(incomingOrder.getLimitPrice()) <= 0;
            case SELL -> restingPrice.compareTo(incomingOrder.getLimitPrice()) >= 0;
        };
    }

    private NavigableMap<BigDecimal, Deque<Order>> oppositeSideFor(Order incomingOrder) {
        return switch (incomingOrder.getSide()) {
            case BUY -> sellOrders;
            case SELL -> buyOrders;
        };
    }

    private NavigableMap<BigDecimal, Deque<Order>> sideFor(Order order) {
        return switch (order.getSide()) {
            case BUY -> buyOrders;
            case SELL -> sellOrders;
        };
    }

    private void validateOrder(Order order) {
        if (order == null) {
            throw new IllegalArgumentException("Order is required");
        }
        if (!symbol.equals(order.getSymbol())) {
            throw new IllegalArgumentException("Order belongs to a different symbol");
        }
    }
}

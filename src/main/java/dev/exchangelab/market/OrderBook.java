package dev.exchangelab.market;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Queue;
import java.util.TreeMap;

@Getter
public class OrderBook {

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    private final String stockSymbol;
    private final NavigableMap<BigDecimal, Queue<Order>> buyOrders;
    private final NavigableMap<BigDecimal, Queue<Order>> sellOrders;

    // ---------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------

    public OrderBook(String stockSymbol) {
        this.stockSymbol = requireText(stockSymbol, "Stock symbol is required");
        this.buyOrders = new TreeMap<>(Comparator.reverseOrder());
        this.sellOrders = new TreeMap<>();
    }

    // ---------------------------------------------------------------------
    // Operations
    // ---------------------------------------------------------------------

    public void add(Order order) {
        Objects.requireNonNull(order);

        if (!stockSymbol.equals(order.getStockSymbol())) {
            throw new IllegalArgumentException("Order stock symbol does not match order book");
        }

        if (order.getSide() == Order.Side.BUY) {
            addToPriceLevel(buyOrders, order);
            return;
        }

        addToPriceLevel(sellOrders, order);
    }

    // ---------------------------------------------------------------------
    // Internal Helpers
    // ---------------------------------------------------------------------

    private static void addToPriceLevel(NavigableMap<BigDecimal, Queue<Order>> orders, Order order) {
        orders.computeIfAbsent(order.getLimitPrice(), ignored -> new ArrayDeque<>()).add(order);
    }

    // ---------------------------------------------------------------------
    // Validation
    // ---------------------------------------------------------------------

    private static String requireText(String value, String message) {
        Objects.requireNonNull(value, message);

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(message);
        }

        return trimmed;
    }
}

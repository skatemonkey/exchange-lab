package dev.exchangelab.market;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Getter
public class OrderBook {

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    private final String stockSymbol;
    private final List<Order> buyOrders;
    private final List<Order> sellOrders;

    // ---------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------

    public OrderBook(String stockSymbol) {
        this.stockSymbol = requireText(stockSymbol, "Stock symbol is required");
        this.buyOrders = new ArrayList<>();
        this.sellOrders = new ArrayList<>();
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
            buyOrders.add(order);
            return;
        }

        sellOrders.add(order);
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

package dev.exchangelab.match.application.orderbook;

import dev.exchangelab.match.domain.model.MatchResult;
import dev.exchangelab.match.domain.model.Order;
import dev.exchangelab.match.domain.model.OrderBook;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InMemoryOrderBookRegistry {

    private final Map<String, OrderBook> orderBooks = new HashMap<>();

    public synchronized MatchResult match(Order incomingOrder) {
        return orderBooks
                .computeIfAbsent(incomingOrder.getSymbol(), OrderBook::new)
                .match(incomingOrder);
    }

    public synchronized void add(Order order) {
        orderBooks
                .computeIfAbsent(order.getSymbol(), OrderBook::new)
                .add(order);
    }

    public synchronized void rebuild(List<Order> openOrders) {
        orderBooks.clear();
        openOrders.stream()
                .filter(Order::isOpen)
                .sorted(Comparator
                        .comparing(Order::getSymbol)
                        .thenComparing(Order::getCreatedAt)
                        .thenComparing(Order::getOrderId))
                .forEach(this::add);
    }

    public synchronized void clear() {
        orderBooks.clear();
    }
}

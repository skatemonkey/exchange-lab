package dev.exchangelab.domain.repository;

import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.OrderBook;

import java.util.List;

public interface OrderRepository {

    OrderBook findOrderBookFor(Order incomingOrder);

    void saveAll(List<Order> orders);
}

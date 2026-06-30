package dev.exchangelab.domain.repository;

import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.OrderBook;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {

    Optional<Order> findById(UUID orderId);

    OrderBook findOrderBookFor(Order incomingOrder);

    void save(Order order);

    void saveAll(List<Order> orders);
}

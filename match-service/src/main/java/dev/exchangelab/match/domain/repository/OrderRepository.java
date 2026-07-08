package dev.exchangelab.match.domain.repository;

import dev.exchangelab.match.domain.model.Order;

import java.util.List;

public interface OrderRepository {

    List<Order> findOpenOrders();

    void saveAll(List<Order> orders);
}

package dev.exchangelab.domain.repository;

import dev.exchangelab.domain.model.Order;

import java.util.List;

public interface OrderRepository {

    List<Order> findOpenOrders();

    void saveAll(List<Order> orders);
}

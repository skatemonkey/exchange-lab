package dev.exchangelab.infrastructure.persistence.query;

import dev.exchangelab.infrastructure.persistence.entity.OrderEntity;

import java.util.List;

public interface OrderQuery {

    List<OrderEntity> findOpenOrders();
}

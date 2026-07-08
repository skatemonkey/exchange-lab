package dev.exchangelab.match.persistence.query;

import dev.exchangelab.match.persistence.entity.OrderEntity;

import java.util.List;

public interface OrderQuery {

    List<OrderEntity> findOpenOrders();
}

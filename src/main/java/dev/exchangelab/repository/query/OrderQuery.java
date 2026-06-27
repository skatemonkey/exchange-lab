package dev.exchangelab.repository.query;

import dev.exchangelab.model.entity.OrderEntity;

import java.util.List;

public interface OrderQuery {

    List<OrderEntity> findAcceptedOrdersBySymbol(String symbol);
}

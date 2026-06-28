package dev.exchangelab.domain.repository;

import dev.exchangelab.domain.model.Order;

import java.math.BigDecimal;
import java.util.List;

public interface OrderRepository {

    List<Order> findMatchableBuyOrders(String symbol, BigDecimal sellLimitPrice);

    List<Order> findMatchableSellOrders(String symbol, BigDecimal buyLimitPrice);

    void saveAll(List<Order> orders);
}

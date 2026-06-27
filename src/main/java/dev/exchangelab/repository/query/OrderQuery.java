package dev.exchangelab.repository.query;

import dev.exchangelab.model.entity.OrderEntity;

import java.math.BigDecimal;
import java.util.List;

public interface OrderQuery {

    List<OrderEntity> findMatchableBuyOrders(String symbol, BigDecimal sellLimitPrice);

    List<OrderEntity> findMatchableSellOrders(String symbol, BigDecimal buyLimitPrice);
}

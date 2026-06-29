package dev.exchangelab.infrastructure.persistence.query;

import dev.exchangelab.infrastructure.persistence.entity.OrderEntity;

import java.math.BigDecimal;
import java.util.List;

public interface OrderQuery {

    List<OrderEntity> findMatchableBuyOrders(String symbol, BigDecimal sellLimitPrice);

    List<OrderEntity> findMatchableSellOrders(String symbol, BigDecimal buyLimitPrice);
}

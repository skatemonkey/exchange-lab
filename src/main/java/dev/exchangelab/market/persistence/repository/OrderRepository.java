package dev.exchangelab.market.persistence.repository;

import dev.exchangelab.market.domain.Order;
import dev.exchangelab.market.persistence.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    List<OrderEntity> findByStockSymbolAndStatus(String stockSymbol, Order.Status status);

    List<OrderEntity> findByTraderId(UUID traderId);
}

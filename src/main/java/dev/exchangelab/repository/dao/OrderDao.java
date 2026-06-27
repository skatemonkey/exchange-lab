package dev.exchangelab.repository.dao;

import dev.exchangelab.model.entity.OrderEntity;
import dev.exchangelab.repository.query.OrderQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderDao extends JpaRepository<OrderEntity, UUID>, OrderQuery {

    List<OrderEntity> findByTraderId(UUID traderId);

    List<OrderEntity> findBySymbol(String symbol);
}

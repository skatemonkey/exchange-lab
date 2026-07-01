package dev.exchangelab.infrastructure.persistence.dao;

import dev.exchangelab.infrastructure.persistence.entity.OrderEntity;
import dev.exchangelab.infrastructure.persistence.query.OrderQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderDao extends JpaRepository<OrderEntity, UUID>, OrderQuery {
}

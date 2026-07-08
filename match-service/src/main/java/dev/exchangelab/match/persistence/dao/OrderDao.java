package dev.exchangelab.match.persistence.dao;

import dev.exchangelab.match.persistence.entity.OrderEntity;
import dev.exchangelab.match.persistence.query.OrderQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderDao extends JpaRepository<OrderEntity, UUID>, OrderQuery {
}

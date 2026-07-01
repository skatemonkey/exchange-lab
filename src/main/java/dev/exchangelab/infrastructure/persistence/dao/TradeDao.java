package dev.exchangelab.infrastructure.persistence.dao;

import dev.exchangelab.infrastructure.persistence.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TradeDao extends JpaRepository<TradeEntity, UUID> {
}

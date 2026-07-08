package dev.exchangelab.match.persistence.dao;

import dev.exchangelab.match.persistence.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TradeDao extends JpaRepository<TradeEntity, UUID> {
}

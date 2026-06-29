package dev.exchangelab.infrastructure.persistence.dao;

import dev.exchangelab.infrastructure.persistence.entity.StockPositionEntity;
import dev.exchangelab.infrastructure.persistence.query.StockPositionQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockPositionDao extends JpaRepository<StockPositionEntity, UUID>, StockPositionQuery {

    Optional<StockPositionEntity> findByTraderIdAndSymbol(UUID traderId, String symbol);

    List<StockPositionEntity> findByTraderId(UUID traderId);
}

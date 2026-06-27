package dev.exchangelab.repository.dao;

import dev.exchangelab.model.entity.StockPositionEntity;
import dev.exchangelab.repository.query.StockPositionQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StockPositionDao extends JpaRepository<StockPositionEntity, UUID>, StockPositionQuery {

    Optional<StockPositionEntity> findByTraderIdAndSymbol(UUID traderId, String symbol);

    List<StockPositionEntity> findByTraderId(UUID traderId);
}

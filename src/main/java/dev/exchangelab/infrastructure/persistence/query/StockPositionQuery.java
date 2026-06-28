package dev.exchangelab.infrastructure.persistence.query;

import dev.exchangelab.infrastructure.persistence.entity.StockPositionEntity;

import java.util.Optional;
import java.util.UUID;

public interface StockPositionQuery {

    Optional<StockPositionEntity> findPositionForStockCheck(UUID traderId, String symbol);
}

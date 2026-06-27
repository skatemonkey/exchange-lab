package dev.exchangelab.repository.query;

import dev.exchangelab.model.entity.StockPositionEntity;

import java.util.Optional;
import java.util.UUID;

public interface StockPositionQuery {

    Optional<StockPositionEntity> findPositionForStockCheck(UUID traderId, String symbol);
}

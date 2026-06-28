package dev.exchangelab.domain.repository;

import dev.exchangelab.domain.model.StockPosition;

import java.util.Optional;
import java.util.UUID;

public interface StockPositionRepository {

    Optional<StockPosition> findForStockReservation(UUID traderId, String symbol);

    void save(StockPosition position);
}

package dev.exchangelab.finance.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StockPositionRepository extends JpaRepository<StockPositionEntity, UUID> {

    Optional<StockPositionEntity> findByTraderIdAndSymbol(UUID traderId, String symbol);
}

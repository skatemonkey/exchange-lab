package dev.exchangelab.market.persistence.repository;

import dev.exchangelab.market.persistence.entity.TradeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TradeRepository extends JpaRepository<TradeEntity, UUID> {

    List<TradeEntity> findByStockSymbol(String stockSymbol);
}

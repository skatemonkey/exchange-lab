package dev.exchangelab.infrastructure.persistence.dao;

import dev.exchangelab.infrastructure.persistence.entity.TradeEntity;
import dev.exchangelab.infrastructure.persistence.query.TradeQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TradeDao extends JpaRepository<TradeEntity, UUID>, TradeQuery {

    List<TradeEntity> findBySymbol(String symbol);

    List<TradeEntity> findByBuyerTraderId(UUID buyerTraderId);

    List<TradeEntity> findBySellerTraderId(UUID sellerTraderId);
}

package dev.exchangelab.market.persistence.repository;

import dev.exchangelab.market.persistence.entity.StockEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<StockEntity, String> {
}

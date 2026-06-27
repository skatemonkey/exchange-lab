package dev.exchangelab.repository.query;

import dev.exchangelab.model.entity.StockPositionEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class StockPositionQueryImpl implements StockPositionQuery {

    private final EntityManager entityManager;

    public StockPositionQueryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<StockPositionEntity> findPositionForStockCheck(UUID traderId, String symbol) {
        return entityManager.createQuery("""
                        select p
                        from StockPositionEntity p
                        where p.traderId = :traderId
                          and p.symbol = :symbol
                        """, StockPositionEntity.class)
                .setParameter("traderId", traderId)
                .setParameter("symbol", symbol)
                .getResultStream()
                .findFirst();
    }
}

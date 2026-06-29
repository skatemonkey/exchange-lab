package dev.exchangelab.infrastructure.persistence.query;

import dev.exchangelab.infrastructure.persistence.entity.TradeEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class TradeQueryImpl implements TradeQuery {

    private final EntityManager entityManager;

    public TradeQueryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<TradeEntity> findLatestTradesBySymbol(String symbol, int limit) {
        return entityManager.createQuery("""
                        select t
                        from TradeEntity t
                        where t.symbol = :symbol
                        order by t.createdAt desc
                        """, TradeEntity.class)
                .setParameter("symbol", symbol)
                .setMaxResults(limit)
                .getResultList();
    }
}

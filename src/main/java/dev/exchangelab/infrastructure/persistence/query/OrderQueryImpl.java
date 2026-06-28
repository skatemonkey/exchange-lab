package dev.exchangelab.infrastructure.persistence.query;

import dev.exchangelab.domain.model.OrderSide;
import dev.exchangelab.domain.model.OrderStatus;
import dev.exchangelab.infrastructure.persistence.entity.OrderEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public class OrderQueryImpl implements OrderQuery {

    private static final List<OrderStatus> MATCHABLE_STATUSES = List.of(
            OrderStatus.ACCEPTED,
            OrderStatus.PARTIALLY_FILLED
    );

    private final EntityManager entityManager;

    public OrderQueryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<OrderEntity> findMatchableBuyOrders(String symbol, BigDecimal sellLimitPrice) {
        // Existing buy orders: highest bid first, then oldest order first.
        return entityManager.createQuery("""
                        select o
                        from OrderEntity o
                        where o.symbol = :symbol
                          and o.status in :matchableStatuses
                          and o.side = :side
                          and o.remainingQuantity > 0
                          and o.limitPrice >= :sellLimitPrice
                        order by o.limitPrice desc, o.createdAt asc
                        """, OrderEntity.class)
                .setParameter("symbol", symbol)
                .setParameter("matchableStatuses", MATCHABLE_STATUSES)
                .setParameter("side", OrderSide.BUY)
                .setParameter("sellLimitPrice", sellLimitPrice)
                .getResultList();
    }

    @Override
    public List<OrderEntity> findMatchableSellOrders(String symbol, BigDecimal buyLimitPrice) {
        // Existing sell orders: lowest ask first, then oldest order first.
        return entityManager.createQuery("""
                        select o
                        from OrderEntity o
                        where o.symbol = :symbol
                          and o.status in :matchableStatuses
                          and o.side = :side
                          and o.remainingQuantity > 0
                          and o.limitPrice <= :buyLimitPrice
                        order by o.limitPrice asc, o.createdAt asc
                        """, OrderEntity.class)
                .setParameter("symbol", symbol)
                .setParameter("matchableStatuses", MATCHABLE_STATUSES)
                .setParameter("side", OrderSide.SELL)
                .setParameter("buyLimitPrice", buyLimitPrice)
                .getResultList();
    }
}

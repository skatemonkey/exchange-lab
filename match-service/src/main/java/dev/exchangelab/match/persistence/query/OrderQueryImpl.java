package dev.exchangelab.match.persistence.query;

import dev.exchangelab.common.order.OrderStatus;

import dev.exchangelab.match.domain.model.Order;
import dev.exchangelab.match.persistence.entity.OrderEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderQueryImpl implements OrderQuery {

    private static final List<OrderStatus> OPEN_STATUSES = List.of(
            OrderStatus.ACCEPTED,
            OrderStatus.PARTIALLY_FILLED
    );

    private final EntityManager entityManager;

    public OrderQueryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<OrderEntity> findOpenOrders() {
        return entityManager.createQuery("""
                        select o
                        from OrderEntity o
                        where o.status in :openStatuses
                          and o.remainingQuantity > 0
                        order by o.symbol asc, o.createdAt asc
                        """, OrderEntity.class)
                .setParameter("openStatuses", OPEN_STATUSES)
                .getResultList();
    }
}

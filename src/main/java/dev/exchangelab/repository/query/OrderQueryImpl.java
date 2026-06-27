package dev.exchangelab.repository.query;

import dev.exchangelab.model.entity.OrderEntity;
import dev.exchangelab.model.enums.OrderStatus;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderQueryImpl implements OrderQuery {

    private final EntityManager entityManager;

    public OrderQueryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public List<OrderEntity> findAcceptedOrdersBySymbol(String symbol) {
        return entityManager.createQuery("""
                        select o
                        from OrderEntity o
                        where o.symbol = :symbol
                          and o.status = :status
                        order by o.createdAt asc
                        """, OrderEntity.class)
                .setParameter("symbol", symbol)
                .setParameter("status", OrderStatus.ACCEPTED)
                .getResultList();
    }
}

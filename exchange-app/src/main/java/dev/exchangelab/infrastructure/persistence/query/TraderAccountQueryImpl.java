package dev.exchangelab.infrastructure.persistence.query;

import dev.exchangelab.infrastructure.persistence.entity.TraderAccountEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class TraderAccountQueryImpl implements TraderAccountQuery {

    private final EntityManager entityManager;

    public TraderAccountQueryImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public Optional<TraderAccountEntity> findAccountForCashCheck(UUID traderId) {
        return entityManager.createQuery("""
                        select a
                        from TraderAccountEntity a
                        where a.traderId = :traderId
                        """, TraderAccountEntity.class)
                .setParameter("traderId", traderId)
                .getResultStream()
                .findFirst();
    }
}

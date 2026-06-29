package dev.exchangelab.infrastructure.persistence.query;

import dev.exchangelab.infrastructure.persistence.entity.TraderAccountEntity;

import java.util.Optional;
import java.util.UUID;

public interface TraderAccountQuery {

    Optional<TraderAccountEntity> findAccountForCashCheck(UUID traderId);
}

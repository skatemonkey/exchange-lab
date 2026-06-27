package dev.exchangelab.repository.query;

import dev.exchangelab.model.entity.TraderAccountEntity;

import java.util.Optional;
import java.util.UUID;

public interface TraderAccountQuery {

    Optional<TraderAccountEntity> findAccountForCashCheck(UUID traderId);
}

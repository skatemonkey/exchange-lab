package dev.exchangelab.domain.repository;

import dev.exchangelab.domain.model.TraderAccount;

import java.util.Optional;
import java.util.UUID;

public interface TraderAccountRepository {

    Optional<TraderAccount> findForCashReservation(UUID traderId);

    void save(TraderAccount account);
}

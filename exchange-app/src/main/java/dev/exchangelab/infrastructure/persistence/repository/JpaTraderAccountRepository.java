package dev.exchangelab.infrastructure.persistence.repository;

import dev.exchangelab.domain.model.TraderAccount;
import dev.exchangelab.domain.repository.TraderAccountRepository;
import dev.exchangelab.infrastructure.persistence.dao.TraderAccountDao;
import dev.exchangelab.infrastructure.persistence.entity.TraderAccountEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaTraderAccountRepository implements TraderAccountRepository {

    private final TraderAccountDao traderAccountDao;

    @Override
    public Optional<TraderAccount> findForCashReservation(UUID traderId) {
        return traderAccountDao.findAccountForCashCheck(traderId).map(this::toDomain);
    }

    @Override
    public void save(TraderAccount account) {
        traderAccountDao.save(toEntity(account));
    }

    private TraderAccount toDomain(TraderAccountEntity entity) {
        return new TraderAccount(
                entity.getTraderId(),
                entity.getCashBalance(),
                entity.getReservedCash()
        );
    }

    private TraderAccountEntity toEntity(TraderAccount account) {
        return new TraderAccountEntity(
                account.getTraderId(),
                account.getCashBalance(),
                account.getReservedCash()
        );
    }
}

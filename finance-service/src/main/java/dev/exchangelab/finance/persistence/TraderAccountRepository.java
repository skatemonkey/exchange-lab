package dev.exchangelab.finance.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface TraderAccountRepository extends JpaRepository<TraderAccountEntity, UUID> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from TraderAccountEntity a where a.traderId = :traderId")
    Optional<TraderAccountEntity> findByIdForUpdate(@Param("traderId") UUID traderId);
}

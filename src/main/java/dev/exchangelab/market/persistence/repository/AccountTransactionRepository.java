package dev.exchangelab.market.persistence.repository;

import dev.exchangelab.market.persistence.entity.AccountTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AccountTransactionRepository extends JpaRepository<AccountTransactionEntity, UUID> {

    List<AccountTransactionEntity> findByTraderId(UUID traderId);
}

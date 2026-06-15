package dev.exchangelab.market.persistence.repository;

import dev.exchangelab.market.persistence.entity.TraderAccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TraderAccountRepository extends JpaRepository<TraderAccountEntity, UUID> {

    Optional<TraderAccountEntity> findByUsername(String username);
}

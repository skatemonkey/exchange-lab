package dev.exchangelab.finance.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TraderAccountRepository extends JpaRepository<TraderAccountEntity, UUID> {
}

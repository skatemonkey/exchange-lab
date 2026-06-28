package dev.exchangelab.infrastructure.persistence.dao;

import dev.exchangelab.infrastructure.persistence.entity.TraderAccountEntity;
import dev.exchangelab.infrastructure.persistence.query.TraderAccountQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TraderAccountDao extends JpaRepository<TraderAccountEntity, UUID>, TraderAccountQuery {
}

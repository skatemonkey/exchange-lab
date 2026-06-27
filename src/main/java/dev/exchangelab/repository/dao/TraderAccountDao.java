package dev.exchangelab.repository.dao;

import dev.exchangelab.model.entity.TraderAccountEntity;
import dev.exchangelab.repository.query.TraderAccountQuery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TraderAccountDao extends JpaRepository<TraderAccountEntity, UUID>, TraderAccountQuery {
}

package dev.exchangelab.market.persistence.repository;

import dev.exchangelab.market.persistence.entity.PositionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PositionRepository extends JpaRepository<PositionEntity, PositionEntity.PositionId> {

    List<PositionEntity> findByIdTraderId(UUID traderId);
}

package dev.exchangelab.infrastructure.persistence.repository;

import dev.exchangelab.domain.model.StockPosition;
import dev.exchangelab.domain.repository.StockPositionRepository;
import dev.exchangelab.infrastructure.persistence.dao.StockPositionDao;
import dev.exchangelab.infrastructure.persistence.entity.StockPositionEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class JpaStockPositionRepository implements StockPositionRepository {

    private final StockPositionDao stockPositionDao;

    @Override
    public Optional<StockPosition> findForStockReservation(UUID traderId, String symbol) {
        return stockPositionDao.findPositionForStockCheck(traderId, symbol).map(this::toDomain);
    }

    @Override
    public void save(StockPosition position) {
        stockPositionDao.save(toEntity(position));
    }

    private StockPosition toDomain(StockPositionEntity entity) {
        return new StockPosition(
                entity.getPositionId(),
                entity.getTraderId(),
                entity.getSymbol(),
                entity.getQuantity(),
                entity.getReservedQuantity()
        );
    }

    private StockPositionEntity toEntity(StockPosition position) {
        return new StockPositionEntity(
                position.getPositionId(),
                position.getTraderId(),
                position.getSymbol(),
                position.getQuantity(),
                position.getReservedQuantity()
        );
    }
}

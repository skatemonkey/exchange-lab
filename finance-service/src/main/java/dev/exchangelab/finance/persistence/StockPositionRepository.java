package dev.exchangelab.finance.persistence;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface StockPositionRepository extends JpaRepository<StockPositionEntity, UUID> {

    Optional<StockPositionEntity> findByTraderIdAndSymbol(UUID traderId, String symbol);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select p from StockPositionEntity p
            where p.traderId = :traderId
              and p.symbol = :symbol
            """)
    Optional<StockPositionEntity> findByTraderIdAndSymbolForUpdate(
            @Param("traderId") UUID traderId,
            @Param("symbol") String symbol
    );
}

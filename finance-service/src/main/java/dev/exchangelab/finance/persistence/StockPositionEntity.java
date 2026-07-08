package dev.exchangelab.finance.persistence;

import dev.exchangelab.finance.domain.StockPosition;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Entity
@Table(name = "stock_positions")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockPositionEntity {

    @Id
    @Column(name = "position_id", nullable = false)
    private UUID positionId;

    @Column(name = "trader_id", nullable = false)
    private UUID traderId;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    @Column(name = "reserved_quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal reservedQuantity;

    public StockPosition toDomain() {
        return new StockPosition(positionId, traderId, symbol, quantity, reservedQuantity);
    }

    public void updateFrom(StockPosition position) {
        quantity = position.getQuantity();
        reservedQuantity = position.getReservedQuantity();
    }
}

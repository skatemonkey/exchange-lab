package dev.exchangelab.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "stock_positions")
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

    protected StockPositionEntity() {
    }

    public StockPositionEntity(
            UUID positionId,
            UUID traderId,
            String symbol,
            BigDecimal quantity,
            BigDecimal reservedQuantity
    ) {
        this.positionId = positionId;
        this.traderId = traderId;
        this.symbol = symbol;
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
    }

    public UUID getPositionId() {
        return positionId;
    }

    public UUID getTraderId() {
        return traderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getReservedQuantity() {
        return reservedQuantity;
    }

    public BigDecimal getAvailableQuantity() {
        return quantity.subtract(reservedQuantity);
    }
}

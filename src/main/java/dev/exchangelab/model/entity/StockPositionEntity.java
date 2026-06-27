package dev.exchangelab.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Setter
    @Column(name = "reserved_quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal reservedQuantity;
}

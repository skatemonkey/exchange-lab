package dev.exchangelab.finance.persistence;

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

    public BigDecimal availableQuantity() {
        return quantity.subtract(reservedQuantity);
    }

    public void reserve(BigDecimal amount) {
        validatePositive(amount);
        if (availableQuantity().compareTo(amount) < 0) {
            throw new IllegalStateException("Trader does not have enough available stock");
        }

        reservedQuantity = reservedQuantity.add(amount);
    }

    public void settleSell(BigDecimal amount) {
        validatePositive(amount);
        if (reservedQuantity.compareTo(amount) < 0) {
            throw new IllegalStateException("Cannot release more reserved stock than available");
        }
        if (quantity.compareTo(amount) < 0) {
            throw new IllegalStateException("Trader does not have enough stock to settle trade");
        }

        quantity = quantity.subtract(amount);
        reservedQuantity = reservedQuantity.subtract(amount);
    }

    public void receive(BigDecimal amount) {
        validatePositive(amount);
        quantity = quantity.add(amount);
    }

    private void validatePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}

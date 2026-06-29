package dev.exchangelab.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class StockPosition {

    private final UUID positionId;
    private final UUID traderId;
    private final String symbol;
    private BigDecimal quantity;
    private BigDecimal reservedQuantity;

    public record Key(UUID traderId, String symbol) {
    }

    public Key key() {
        return new Key(traderId, symbol);
    }

    public BigDecimal availableQuantity() {
        return quantity.subtract(reservedQuantity);
    }

    public void reserve(BigDecimal amount) {
        if (availableQuantity().compareTo(amount) < 0) {
            throw new IllegalStateException("Trader does not have enough available stock");
        }

        reservedQuantity = reservedQuantity.add(amount);
    }

    public void settleSell(BigDecimal amount) {
        if (reservedQuantity.compareTo(amount) < 0) {
            throw new IllegalStateException("Cannot release more reserved stock than available");
        }

        quantity = quantity.subtract(amount);
        reservedQuantity = reservedQuantity.subtract(amount);
    }

    public void receive(BigDecimal amount) {
        quantity = quantity.add(amount);
    }
}

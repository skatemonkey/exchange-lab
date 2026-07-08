package dev.exchangelab.finance.domain;

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

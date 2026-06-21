package dev.exchangelab.market.domain;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Position {

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    private final UUID traderId;
    private final String stockSymbol;
    private final BigDecimal quantity;
    private final BigDecimal reservedQuantity;

    // ---------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------

    public Position(
            UUID traderId,
            String stockSymbol,
            BigDecimal quantity,
            BigDecimal reservedQuantity
    ) {
        if (quantity == null || quantity.signum() < 0) {
            throw new IllegalArgumentException("Position quantity cannot be negative");
        }
        if (reservedQuantity == null || reservedQuantity.signum() < 0) {
            throw new IllegalArgumentException("Reserved quantity cannot be negative");
        }
        if (reservedQuantity.compareTo(quantity) > 0) {
            throw new IllegalArgumentException("Reserved quantity cannot exceed position quantity");
        }

        this.traderId = Objects.requireNonNull(traderId);
        this.stockSymbol = requireText(stockSymbol, "Stock symbol is required");
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
    }

    // ---------------------------------------------------------------------
    // Derived State
    // ---------------------------------------------------------------------

    public BigDecimal getAvailableQuantity() {
        return quantity.subtract(reservedQuantity);
    }

    // ---------------------------------------------------------------------
    // Validation
    // ---------------------------------------------------------------------

    private static String requireText(String value, String message) {
        Objects.requireNonNull(value, message);

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(message);
        }

        return trimmed;
    }
}

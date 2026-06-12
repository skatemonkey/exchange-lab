package dev.exchangelab.market;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Order {

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    private final UUID orderId;
    private final UUID traderId;
    private final String stockSymbol;
    private final Side side;
    private final BigDecimal limitPrice;
    private final BigDecimal quantity;
    private final Instant submittedAt;

    // ---------------------------------------------------------------------
    // Internal Constructor
    // ---------------------------------------------------------------------

    private Order(
            UUID orderId,
            UUID traderId,
            String stockSymbol,
            Side side,
            BigDecimal limitPrice,
            BigDecimal quantity,
            Instant submittedAt
    ) {
        if (limitPrice == null || limitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Limit price must be greater than zero");
        }
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("Order quantity must be greater than zero");
        }

        this.orderId = Objects.requireNonNull(orderId);
        this.traderId = Objects.requireNonNull(traderId);
        this.stockSymbol = Objects.requireNonNull(stockSymbol);
        this.side = Objects.requireNonNull(side);
        this.limitPrice = limitPrice;
        this.quantity = quantity;
        this.submittedAt = Objects.requireNonNull(submittedAt);
    }

    // ---------------------------------------------------------------------
    // Operations
    // ---------------------------------------------------------------------

    public static Order place(
            UUID traderId,
            String stockSymbol,
            Side side,
            BigDecimal limitPrice,
            BigDecimal quantity
    ) {
        return new Order(
                UUID.randomUUID(),
                traderId,
                stockSymbol,
                side,
                limitPrice,
                quantity,
                Instant.now()
        );
    }

    // ---------------------------------------------------------------------
    // Types
    // ---------------------------------------------------------------------

    public enum Side {
        BUY,
        SELL
    }
}

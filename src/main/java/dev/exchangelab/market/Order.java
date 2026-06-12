package dev.exchangelab.market;

import lombok.Getter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
@Accessors(fluent = true)
public class Order {

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    private final UUID orderId;
    private final UUID traderId;
    private final String stockSymbol;
    private final Side side;
    private final BigDecimal limitPrice;
    private final long quantity;
    private final Instant submittedAt;

    private long filledQuantity;
    private Status status;

    // ---------------------------------------------------------------------
    // Construction
    // ---------------------------------------------------------------------

    public Order(
            UUID orderId,
            UUID traderId,
            String stockSymbol,
            Side side,
            BigDecimal limitPrice,
            long quantity,
            Instant submittedAt
    ) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Order quantity must be greater than zero");
        }
        if (limitPrice == null || limitPrice.signum() <= 0) {
            throw new IllegalArgumentException("Limit price must be greater than zero");
        }

        this.orderId = Objects.requireNonNull(orderId);
        this.traderId = Objects.requireNonNull(traderId);
        this.stockSymbol = Objects.requireNonNull(stockSymbol);
        this.side = Objects.requireNonNull(side);
        this.limitPrice = limitPrice;
        this.quantity = quantity;
        this.submittedAt = Objects.requireNonNull(submittedAt);
        this.filledQuantity = 0;
        this.status = Status.SUBMITTED;
    }

    // ---------------------------------------------------------------------
    // Derived State
    // ---------------------------------------------------------------------

    public long remainingQuantity() {
        return quantity - filledQuantity;
    }

    public boolean isOpen() {
        return status == Status.OPEN || status == Status.PARTIALLY_FILLED;
    }

    // ---------------------------------------------------------------------
    // State Changes
    // ---------------------------------------------------------------------

    public void accept() {
        if (status != Status.SUBMITTED) {
            throw new IllegalStateException("Only submitted orders can be accepted");
        }
        status = Status.OPEN;
    }

    public void reject() {
        if (status != Status.SUBMITTED) {
            throw new IllegalStateException("Only submitted orders can be rejected");
        }
        status = Status.REJECTED;
    }

    public void fill(long quantityToFill) {
        if (!isOpen()) {
            throw new IllegalStateException("Only open orders can be filled");
        }
        if (quantityToFill <= 0) {
            throw new IllegalArgumentException("Fill quantity must be greater than zero");
        }
        if (quantityToFill > remainingQuantity()) {
            throw new IllegalArgumentException("Fill quantity cannot exceed remaining quantity");
        }

        filledQuantity += quantityToFill;
        status = remainingQuantity() == 0 ? Status.FILLED : Status.PARTIALLY_FILLED;
    }

    // ---------------------------------------------------------------------
    // Types
    // ---------------------------------------------------------------------

    public enum Side {
        BUY,
        SELL
    }

    public enum Status {
        SUBMITTED,
        REJECTED,
        OPEN,
        PARTIALLY_FILLED,
        FILLED
    }
}

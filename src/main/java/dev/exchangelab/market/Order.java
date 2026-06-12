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

    private BigDecimal filledQuantity;
    private Status status;

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
        this.filledQuantity = BigDecimal.ZERO;
        this.status = Status.SUBMITTED;
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

    public void fill(BigDecimal quantityToFill) {
        if (!isOpen()) {
            throw new IllegalStateException("Only open orders can be filled");
        }
        if (quantityToFill == null || quantityToFill.signum() <= 0) {
            throw new IllegalArgumentException("Fill quantity must be greater than zero");
        }
        if (quantityToFill.compareTo(getRemainingQuantity()) > 0) {
            throw new IllegalArgumentException("Fill quantity cannot exceed remaining quantity");
        }

        filledQuantity = filledQuantity.add(quantityToFill);
        status = getRemainingQuantity().signum() == 0 ? Status.FILLED : Status.PARTIALLY_FILLED;
    }

    // ---------------------------------------------------------------------
    // Derived State
    // ---------------------------------------------------------------------

    public BigDecimal getRemainingQuantity() {
        return quantity.subtract(filledQuantity);
    }

    public boolean isOpen() {
        return status == Status.OPEN || status == Status.PARTIALLY_FILLED;
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

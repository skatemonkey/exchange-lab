package dev.exchangelab.domain.model;

import dev.exchangelab.common.order.OrderSide;
import dev.exchangelab.common.order.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Order {

    private final UUID orderId;
    private final UUID traderId;
    private final String symbol;
    private final OrderSide side;
    private final BigDecimal limitPrice;
    private final BigDecimal quantity;
    private BigDecimal remainingQuantity;
    private OrderStatus status;
    private final Instant createdAt;

    public static Order createLimit(
            UUID traderId,
            String symbol,
            OrderSide side,
            BigDecimal limitPrice,
            BigDecimal quantity
    ) {
        return createLimit(
                UUID.randomUUID(),
                traderId,
                symbol,
                side,
                limitPrice,
                quantity,
                Instant.now()
        );
    }

    public static Order createLimit(
            UUID orderId,
            UUID traderId,
            String symbol,
            OrderSide side,
            BigDecimal limitPrice,
            BigDecimal quantity,
            Instant createdAt
    ) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order id is required");
        }
        if (traderId == null) {
            throw new IllegalArgumentException("Trader id is required");
        }
        if (symbol == null || symbol.isBlank()) {
            throw new IllegalArgumentException("Stock symbol is required");
        }
        if (side == null) {
            throw new IllegalArgumentException("Order side is required");
        }
        if (limitPrice == null || limitPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Limit price must be greater than zero");
        }
        if (quantity == null || quantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        if (createdAt == null) {
            throw new IllegalArgumentException("Created at is required");
        }

        return new Order(
                orderId,
                traderId,
                symbol,
                side,
                limitPrice,
                quantity,
                quantity,
                OrderStatus.ACCEPTED,
                createdAt
        );
    }

    public void fill(BigDecimal filledQuantity) {
        if (filledQuantity == null || filledQuantity.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Filled quantity must be greater than zero");
        }
        if (filledQuantity.compareTo(remainingQuantity) > 0) {
            throw new IllegalStateException("Cannot fill more than remaining quantity");
        }

        remainingQuantity = remainingQuantity.subtract(filledQuantity);
        refreshStatus();
    }

    public boolean isOpen() {
        return status != OrderStatus.FILLED && remainingQuantity.compareTo(BigDecimal.ZERO) > 0;
    }

    private void refreshStatus() {
        if (remainingQuantity.compareTo(BigDecimal.ZERO) == 0) {
            status = OrderStatus.FILLED;
            return;
        }

        if (remainingQuantity.compareTo(quantity) < 0) {
            status = OrderStatus.PARTIALLY_FILLED;
            return;
        }

        status = OrderStatus.ACCEPTED;
    }
}

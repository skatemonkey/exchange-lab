package dev.exchangelab.domain.model;

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

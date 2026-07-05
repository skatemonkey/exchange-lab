package dev.exchangelab.application.event;

import dev.exchangelab.domain.model.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LimitOrderSubmittedEvent(
        UUID orderId,
        UUID traderId,
        String symbol,
        Order.Side side,
        BigDecimal limitPrice,
        BigDecimal quantity,
        Instant submittedAt
) {

    public static LimitOrderSubmittedEvent from(Order order) {
        return new LimitOrderSubmittedEvent(
                order.getOrderId(),
                order.getTraderId(),
                order.getSymbol(),
                order.getSide(),
                order.getLimitPrice(),
                order.getQuantity(),
                order.getCreatedAt()
        );
    }
}

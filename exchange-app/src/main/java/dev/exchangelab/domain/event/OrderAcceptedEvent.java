package dev.exchangelab.domain.event;

import dev.exchangelab.domain.model.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record OrderAcceptedEvent(
        UUID orderId,
        UUID traderId,
        String symbol,
        Order.Side side,
        BigDecimal limitPrice,
        BigDecimal quantity,
        Instant acceptedAt
) {

    public static OrderAcceptedEvent from(Order order) {
        return new OrderAcceptedEvent(
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

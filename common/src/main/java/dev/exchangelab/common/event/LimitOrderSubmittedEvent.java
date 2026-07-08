package dev.exchangelab.common.event;

import dev.exchangelab.common.order.OrderSide;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record LimitOrderSubmittedEvent(
        UUID orderId,
        UUID traderId,
        String symbol,
        OrderSide side,
        BigDecimal limitPrice,
        BigDecimal quantity,
        BigDecimal reservedCash,
        BigDecimal reservedStock,
        Instant submittedAt
) {
}

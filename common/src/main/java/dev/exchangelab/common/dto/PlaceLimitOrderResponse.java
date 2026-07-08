package dev.exchangelab.common.dto;

import dev.exchangelab.common.order.OrderSide;
import dev.exchangelab.common.order.OrderStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PlaceLimitOrderResponse(
        UUID orderId,
        UUID traderId,
        String symbol,
        OrderSide side,
        BigDecimal limitPrice,
        BigDecimal quantity,
        OrderStatus status
) {
}

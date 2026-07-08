package dev.exchangelab.common.dto;

import dev.exchangelab.common.order.OrderSide;

import java.math.BigDecimal;
import java.util.UUID;

public record PlaceLimitOrderRequest(
        UUID traderId,
        String symbol,
        OrderSide side,
        BigDecimal limitPrice,
        BigDecimal quantity
) {
}

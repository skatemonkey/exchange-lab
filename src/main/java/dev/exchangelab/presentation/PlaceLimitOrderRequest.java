package dev.exchangelab.presentation;

import dev.exchangelab.domain.model.OrderSide;

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

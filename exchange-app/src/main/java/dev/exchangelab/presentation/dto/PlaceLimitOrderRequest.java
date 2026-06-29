package dev.exchangelab.presentation.dto;

import dev.exchangelab.domain.model.Order;

import java.math.BigDecimal;
import java.util.UUID;

public record PlaceLimitOrderRequest(
        UUID traderId,
        String symbol,
        Order.Side side,
        BigDecimal limitPrice,
        BigDecimal quantity
) {
}

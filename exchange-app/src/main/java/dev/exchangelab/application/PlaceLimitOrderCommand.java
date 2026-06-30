package dev.exchangelab.application;

import dev.exchangelab.domain.model.Order;

import java.math.BigDecimal;
import java.util.UUID;

public record PlaceLimitOrderCommand(
        UUID traderId,
        String symbol,
        Order.Side side,
        BigDecimal limitPrice,
        BigDecimal quantity
) {
}

package dev.exchangelab.market.application.dto;

import dev.exchangelab.market.Order;

import java.math.BigDecimal;
import java.util.UUID;

public record PlaceLimitOrderCommand(
        UUID traderId,
        String stockSymbol,
        Order.Side side,
        BigDecimal limitPrice,
        BigDecimal quantity
) {
}

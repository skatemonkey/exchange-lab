package dev.exchangelab.model.dto;

import dev.exchangelab.model.enums.OrderSide;

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

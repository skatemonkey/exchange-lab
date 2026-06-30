package dev.exchangelab.presentation.dto;

import dev.exchangelab.application.PlaceLimitOrderResult;
import dev.exchangelab.domain.model.Order;

import java.math.BigDecimal;
import java.util.UUID;

public record PlaceLimitOrderResponse(
        UUID orderId,
        UUID traderId,
        String symbol,
        Order.Side side,
        BigDecimal limitPrice,
        BigDecimal quantity,
        Order.Status status
) {

    public static PlaceLimitOrderResponse from(PlaceLimitOrderResult result) {
        return new PlaceLimitOrderResponse(
                result.orderId(),
                result.traderId(),
                result.symbol(),
                result.side(),
                result.limitPrice(),
                result.quantity(),
                result.status()
        );
    }
}

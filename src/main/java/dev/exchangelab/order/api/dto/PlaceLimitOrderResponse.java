package dev.exchangelab.order.api.dto;

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

    public static PlaceLimitOrderResponse accepted(
            UUID orderId,
            PlaceLimitOrderRequest request
    ) {
        return new PlaceLimitOrderResponse(
                orderId,
                request.traderId(),
                request.symbol(),
                request.side(),
                request.limitPrice(),
                request.quantity(),
                OrderStatus.ACCEPTED
        );
    }
}

package dev.exchangelab.presentation;

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
                Order.Status.ACCEPTED
        );
    }

    public static PlaceLimitOrderResponse from(Order order) {
        return new PlaceLimitOrderResponse(
                order.getOrderId(),
                order.getTraderId(),
                order.getSymbol(),
                order.getSide(),
                order.getLimitPrice(),
                order.getQuantity(),
                order.getStatus()
        );
    }
}

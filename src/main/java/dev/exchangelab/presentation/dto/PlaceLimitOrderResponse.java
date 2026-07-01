package dev.exchangelab.presentation.dto;

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

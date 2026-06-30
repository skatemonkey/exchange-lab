package dev.exchangelab.application;

import dev.exchangelab.domain.model.Order;

import java.math.BigDecimal;
import java.util.UUID;

public record PlaceLimitOrderResult(
        UUID orderId,
        UUID traderId,
        String symbol,
        Order.Side side,
        BigDecimal limitPrice,
        BigDecimal quantity,
        Order.Status status
) {

    public static PlaceLimitOrderResult from(Order order) {
        return new PlaceLimitOrderResult(
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

package dev.exchangelab.market.application.dto;

import dev.exchangelab.market.Order;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PlaceLimitOrderResult(
        UUID orderId,
        UUID traderId,
        String stockSymbol,
        Order.Side side,
        BigDecimal limitPrice,
        BigDecimal quantity,
        BigDecimal remainingQuantity,
        Order.Status status,
        Instant submittedAt
) {

    public static PlaceLimitOrderResult from(Order order) {
        return new PlaceLimitOrderResult(
                order.getOrderId(),
                order.getTraderId(),
                order.getStockSymbol(),
                order.getSide(),
                order.getLimitPrice(),
                order.getQuantity(),
                order.getRemainingQuantity(),
                order.getStatus(),
                order.getSubmittedAt()
        );
    }
}

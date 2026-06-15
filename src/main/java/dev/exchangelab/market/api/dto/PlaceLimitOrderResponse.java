package dev.exchangelab.market.api.dto;

import dev.exchangelab.market.Order;
import dev.exchangelab.market.application.dto.PlaceLimitOrderResult;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PlaceLimitOrderResponse(
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

    public static PlaceLimitOrderResponse from(PlaceLimitOrderResult result) {
        return new PlaceLimitOrderResponse(
                result.orderId(),
                result.traderId(),
                result.stockSymbol(),
                result.side(),
                result.limitPrice(),
                result.quantity(),
                result.remainingQuantity(),
                result.status(),
                result.submittedAt()
        );
    }
}

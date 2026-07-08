package dev.exchangelab.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record TradeMatchedEvent(
        UUID tradeId,
        UUID buyOrderId,
        UUID sellOrderId,
        UUID buyerTraderId,
        UUID sellerTraderId,
        String symbol,
        BigDecimal price,
        BigDecimal quantity,
        BigDecimal buyOrderLimitPrice,
        Instant matchedAt
) {
}

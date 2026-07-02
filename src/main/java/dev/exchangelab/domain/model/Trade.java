package dev.exchangelab.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Trade {

    private final UUID tradeId;
    private final UUID buyOrderId;
    private final UUID sellOrderId;
    private final UUID buyerTraderId;
    private final UUID sellerTraderId;
    private final String symbol;
    private final BigDecimal price;
    private final BigDecimal quantity;
    private final Instant createdAt;

    public static Trade create(
            Order incomingOrder,
            Order matchingOrder,
            BigDecimal tradeQuantity
    ) {
        Order buyOrder = incomingOrder.getSide() == Order.Side.BUY ? incomingOrder : matchingOrder;
        Order sellOrder = incomingOrder.getSide() == Order.Side.SELL ? incomingOrder : matchingOrder;

        return new Trade(
                UUID.randomUUID(),
                buyOrder.getOrderId(),
                sellOrder.getOrderId(),
                buyOrder.getTraderId(),
                sellOrder.getTraderId(),
                incomingOrder.getSymbol(),
                matchingOrder.getLimitPrice(),
                tradeQuantity,
                Instant.now()
        );
    }
}

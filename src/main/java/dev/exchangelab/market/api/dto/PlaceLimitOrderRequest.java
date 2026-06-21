package dev.exchangelab.market.api.dto;

import dev.exchangelab.market.domain.Order;
import dev.exchangelab.market.application.dto.PlaceLimitOrderCommand;

import java.math.BigDecimal;
import java.util.UUID;

public record PlaceLimitOrderRequest(
        UUID traderId,
        String stockSymbol,
        Order.Side side,
        BigDecimal limitPrice,
        BigDecimal quantity
) {

    public PlaceLimitOrderCommand toCommand() {
        return new PlaceLimitOrderCommand(
                traderId,
                stockSymbol,
                side,
                limitPrice,
                quantity
        );
    }
}

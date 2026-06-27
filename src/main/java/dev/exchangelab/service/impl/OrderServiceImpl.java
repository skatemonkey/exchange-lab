package dev.exchangelab.service.impl;

import dev.exchangelab.model.dto.PlaceLimitOrderRequest;
import dev.exchangelab.model.dto.PlaceLimitOrderResponse;
import dev.exchangelab.service.OrderService;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Override
    public PlaceLimitOrderResponse placeLimitOrder(PlaceLimitOrderRequest request) {
        /*
         * Planned order flow:
         * 1. Validate the limit order request.
         * 2. Check whether the trader has enough cash or stock.
         * 3. Reserve cash for buy orders or stock quantity for sell orders.
         * 4. Compare the order against the current order book.
         * 5. If matching orders exist, execute one or more trades.
         * 6. Settle each trade by moving cash and stock between traders.
         * 7. Store the order, trades, account updates, and position updates.
         * 8. Return the final order status to the client.
         */

        // 1. Validate the limit order request.
        validateLimitOrderRequest(request);

        // 2. Check whether the trader has enough cash or stock.
        checkWhetherTraderHasEnoughCashOrStock(request);

        // 3. Reserve cash for buy orders or stock quantity for sell orders.

        // 4. Compare the order against the current order book.

        // 5. If matching orders exist, execute one or more trades.

        // 6. Settle each trade by moving cash and stock between traders.

        // 7. Store the order, trades, account updates, and position updates.

        // 8. Return the final order status to the client.
        return PlaceLimitOrderResponse.accepted(
                UUID.randomUUID(),
                request
        );
    }

    private void validateLimitOrderRequest(PlaceLimitOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request is required");
        }
        if (request.traderId() == null) {
            throw new IllegalArgumentException("Trader id is required");
        }
        if (request.symbol() == null || request.symbol().isBlank()) {
            throw new IllegalArgumentException("Stock symbol is required");
        }
        if (request.side() == null) {
            throw new IllegalArgumentException("Order side is required");
        }
        if (request.limitPrice() == null || request.limitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Limit price must be greater than zero");
        }
        if (request.quantity() == null || request.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }

    private void checkWhetherTraderHasEnoughCashOrStock(PlaceLimitOrderRequest request) {
        switch (request.side()) {
            case BUY -> {
                BigDecimal requiredCash = request.limitPrice().multiply(request.quantity());
                // Later: load trader account and check available cash >= requiredCash.
            }
            case SELL -> {
                BigDecimal requiredQuantity = request.quantity();
                // Later: load trader position and check available quantity >= requiredQuantity.
            }
        }
    }
}

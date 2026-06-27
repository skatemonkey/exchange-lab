package dev.exchangelab.order.service.impl;

import dev.exchangelab.order.api.dto.PlaceLimitOrderRequest;
import dev.exchangelab.order.api.dto.PlaceLimitOrderResponse;
import dev.exchangelab.order.service.OrderService;
import org.springframework.stereotype.Service;

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

        // 2. Check whether the trader has enough cash or stock.

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
}

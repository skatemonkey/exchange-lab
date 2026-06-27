package dev.exchangelab.order.service;

import dev.exchangelab.order.api.dto.PlaceLimitOrderRequest;
import dev.exchangelab.order.api.dto.PlaceLimitOrderResponse;

public interface OrderService {

    PlaceLimitOrderResponse placeLimitOrder(PlaceLimitOrderRequest request);
}

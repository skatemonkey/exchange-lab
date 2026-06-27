package dev.exchangelab.service;

import dev.exchangelab.model.dto.PlaceLimitOrderRequest;
import dev.exchangelab.model.dto.PlaceLimitOrderResponse;

public interface OrderService {

    PlaceLimitOrderResponse placeLimitOrder(PlaceLimitOrderRequest request);
}

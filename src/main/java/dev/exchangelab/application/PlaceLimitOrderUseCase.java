package dev.exchangelab.application;

import dev.exchangelab.common.dto.PlaceLimitOrderRequest;
import dev.exchangelab.common.dto.PlaceLimitOrderResponse;

public interface PlaceLimitOrderUseCase {

    PlaceLimitOrderResponse placeLimitOrder(PlaceLimitOrderRequest request);
}

package dev.exchangelab.application;

import dev.exchangelab.presentation.dto.PlaceLimitOrderRequest;
import dev.exchangelab.presentation.dto.PlaceLimitOrderResponse;

public interface PlaceLimitOrderUseCase {

    PlaceLimitOrderResponse placeLimitOrder(PlaceLimitOrderRequest request);
}

package dev.exchangelab.application;

import dev.exchangelab.presentation.PlaceLimitOrderRequest;
import dev.exchangelab.presentation.PlaceLimitOrderResponse;

public interface PlaceLimitOrderUseCase {

    PlaceLimitOrderResponse placeLimitOrder(PlaceLimitOrderRequest request);
}

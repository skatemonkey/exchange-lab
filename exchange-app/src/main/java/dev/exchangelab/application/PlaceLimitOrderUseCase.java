package dev.exchangelab.application;

public interface PlaceLimitOrderUseCase {

    PlaceLimitOrderResult placeLimitOrder(PlaceLimitOrderCommand command);
}

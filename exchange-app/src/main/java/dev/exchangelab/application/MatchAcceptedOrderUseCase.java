package dev.exchangelab.application;

import java.util.UUID;

public interface MatchAcceptedOrderUseCase {

    void matchAcceptedOrder(UUID orderId);
}

package dev.exchangelab.application;

import dev.exchangelab.application.event.LimitOrderSubmittedEvent;
import dev.exchangelab.domain.model.Order;
import dev.exchangelab.presentation.dto.PlaceLimitOrderRequest;
import dev.exchangelab.presentation.dto.PlaceLimitOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlaceLimitOrderUseCaseImpl implements PlaceLimitOrderUseCase {

    private final OrderEventPublisher orderEventPublisher;

    @Override
    public PlaceLimitOrderResponse placeLimitOrder(PlaceLimitOrderRequest request) {
        Order incomingOrder = Order.createLimit(
                request.traderId(),
                request.symbol(),
                request.side(),
                request.limitPrice(),
                request.quantity()
        );

        orderEventPublisher.publish(LimitOrderSubmittedEvent.from(incomingOrder));
        return PlaceLimitOrderResponse.from(incomingOrder);
    }
}

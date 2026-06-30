package dev.exchangelab.application;

import dev.exchangelab.domain.event.OrderAcceptedEvent;
import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.Portfolio;
import dev.exchangelab.domain.repository.OrderRepository;
import dev.exchangelab.presentation.dto.PlaceLimitOrderRequest;
import dev.exchangelab.presentation.dto.PlaceLimitOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceLimitOrderUseCaseImpl implements PlaceLimitOrderUseCase {

    private final OrderRepository orderRepository;
    private final PortfolioStore portfolioStore;
    private final OrderAcceptedEventPublisher orderAcceptedEventPublisher;

    @Override
    @Transactional
    public PlaceLimitOrderResponse placeLimitOrder(PlaceLimitOrderRequest request) {
        requireRequest(request);

        Order incomingOrder = Order.createLimit(
                request.traderId(),
                request.symbol(),
                request.side(),
                request.limitPrice(),
                request.quantity()
        );
        Portfolio portfolio = portfolioStore.loadFor(incomingOrder);
        portfolio.reserveFor(incomingOrder);
        portfolioStore.save(portfolio);

        orderRepository.save(incomingOrder);
        orderAcceptedEventPublisher.publish(OrderAcceptedEvent.from(incomingOrder));

        return PlaceLimitOrderResponse.from(incomingOrder);
    }

    private void requireRequest(PlaceLimitOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request is required");
        }
    }
}

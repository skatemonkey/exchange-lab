package dev.exchangelab.application;

import dev.exchangelab.domain.event.OrderAcceptedEvent;
import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.Portfolio;
import dev.exchangelab.domain.repository.OrderRepository;
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
    public PlaceLimitOrderResult placeLimitOrder(PlaceLimitOrderCommand command) {
        requireCommand(command);

        Order incomingOrder = Order.createLimit(
                command.traderId(),
                command.symbol(),
                command.side(),
                command.limitPrice(),
                command.quantity()
        );
        Portfolio portfolio = portfolioStore.loadFor(incomingOrder);
        portfolio.reserveFor(incomingOrder);
        portfolioStore.save(portfolio);

        orderRepository.save(incomingOrder);
        orderAcceptedEventPublisher.publish(OrderAcceptedEvent.from(incomingOrder));

        return PlaceLimitOrderResult.from(incomingOrder);
    }

    private void requireCommand(PlaceLimitOrderCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("Order command is required");
        }
    }
}

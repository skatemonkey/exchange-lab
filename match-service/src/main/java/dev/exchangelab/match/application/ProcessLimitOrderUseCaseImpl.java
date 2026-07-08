package dev.exchangelab.match.application;

import dev.exchangelab.common.event.LimitOrderSubmittedEvent;
import dev.exchangelab.match.application.orderbook.InMemoryOrderBookRegistry;
import dev.exchangelab.match.domain.model.MatchResult;
import dev.exchangelab.match.domain.model.Order;
import dev.exchangelab.match.domain.model.Trade;
import dev.exchangelab.match.domain.repository.OrderRepository;
import dev.exchangelab.match.domain.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProcessLimitOrderUseCaseImpl implements ProcessLimitOrderUseCase {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final InMemoryOrderBookRegistry inMemoryOrderBookRegistry;

    @Override
    @Transactional
    public Order process(LimitOrderSubmittedEvent event) {
        Order incomingOrder = Order.createLimit(
                event.orderId(),
                event.traderId(),
                event.symbol(),
                event.side(),
                event.limitPrice(),
                event.quantity(),
                event.submittedAt()
        );

        MatchResult matchResult = inMemoryOrderBookRegistry.match(incomingOrder);
        List<Order> updatedMatchingOrders = matchResult.updatedRestingOrders();
        List<Trade> executedTrades = matchResult.trades();

        List<Order> ordersToSave = new ArrayList<>();
        ordersToSave.add(incomingOrder);
        ordersToSave.addAll(updatedMatchingOrders);

        orderRepository.saveAll(ordersToSave);
        tradeRepository.saveAll(executedTrades);

        return incomingOrder;
    }
}

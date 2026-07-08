package dev.exchangelab.match.application;

import dev.exchangelab.common.event.LimitOrderSubmittedEvent;
import dev.exchangelab.common.event.TradeMatchedEvent;
import dev.exchangelab.match.application.orderbook.InMemoryOrderBookRegistry;
import dev.exchangelab.match.domain.model.MatchResult;
import dev.exchangelab.match.domain.model.Order;
import dev.exchangelab.match.domain.model.Trade;
import dev.exchangelab.match.domain.repository.OrderRepository;
import dev.exchangelab.match.domain.repository.TradeRepository;
import dev.exchangelab.match.kafka.TradeEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProcessLimitOrderUseCaseImpl implements ProcessLimitOrderUseCase {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final InMemoryOrderBookRegistry inMemoryOrderBookRegistry;
    private final TradeEventPublisher tradeEventPublisher;

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
        publishTradeEvents(executedTrades, ordersToSave);

        return incomingOrder;
    }

    private void publishTradeEvents(List<Trade> trades, List<Order> orders) {
        Map<UUID, Order> ordersById = new HashMap<>();
        orders.forEach(order -> ordersById.put(order.getOrderId(), order));

        trades.stream()
                .map(trade -> toTradeMatchedEvent(trade, ordersById))
                .forEach(tradeEventPublisher::publish);
    }

    private TradeMatchedEvent toTradeMatchedEvent(Trade trade, Map<UUID, Order> ordersById) {
        Order buyOrder = ordersById.get(trade.getBuyOrderId());
        if (buyOrder == null) {
            throw new IllegalStateException("Buy order not found for trade event");
        }

        return new TradeMatchedEvent(
                trade.getTradeId(),
                trade.getBuyOrderId(),
                trade.getSellOrderId(),
                trade.getBuyerTraderId(),
                trade.getSellerTraderId(),
                trade.getSymbol(),
                trade.getPrice(),
                trade.getQuantity(),
                buyOrder.getLimitPrice(),
                trade.getCreatedAt()
        );
    }
}

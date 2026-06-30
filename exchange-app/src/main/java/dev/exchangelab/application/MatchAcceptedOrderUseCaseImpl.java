package dev.exchangelab.application;

import dev.exchangelab.domain.model.MatchResult;
import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.OrderBook;
import dev.exchangelab.domain.model.Settlement;
import dev.exchangelab.domain.repository.OrderRepository;
import dev.exchangelab.domain.repository.TradeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchAcceptedOrderUseCaseImpl implements MatchAcceptedOrderUseCase {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final SettlementStore settlementStore;

    @Override
    @Transactional
    public void matchAcceptedOrder(UUID orderId) {
        Order incomingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException("Accepted order not found"));

        if (!canMatch(incomingOrder)) {
            return;
        }

        OrderBook orderBook = orderRepository.findOrderBookFor(incomingOrder);
        MatchResult matchResult = orderBook.match(incomingOrder);

        if (matchResult.executedTrades().isEmpty()) {
            return;
        }

        Settlement settlement = settlementStore.loadFor(matchResult);
        settlement.settle(matchResult);
        settlementStore.save(settlement);

        orderRepository.saveAll(matchResult.ordersToSave());
        tradeRepository.saveAll(matchResult.executedTrades());
    }

    private boolean canMatch(Order order) {
        return order.getRemainingQuantity().compareTo(BigDecimal.ZERO) > 0
                && order.getStatus() != Order.Status.FILLED;
    }
}

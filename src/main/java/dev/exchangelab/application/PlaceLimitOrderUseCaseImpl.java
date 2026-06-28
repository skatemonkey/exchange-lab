package dev.exchangelab.application;

import dev.exchangelab.domain.model.MatchResult;
import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.OrderBook;
import dev.exchangelab.domain.model.Trade;
import dev.exchangelab.domain.repository.OrderRepository;
import dev.exchangelab.domain.repository.TradeRepository;
import dev.exchangelab.domain.service.OrderReservationService;
import dev.exchangelab.domain.service.TradeSettlementService;
import dev.exchangelab.presentation.PlaceLimitOrderRequest;
import dev.exchangelab.presentation.PlaceLimitOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaceLimitOrderUseCaseImpl implements PlaceLimitOrderUseCase {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final OrderReservationService orderReservationService;
    private final TradeSettlementService tradeSettlementService;

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
        orderReservationService.reserve(incomingOrder);

        List<Order> matchingOrderList = findMatchingOrderList(incomingOrder);
        OrderBook orderBook = new OrderBook(matchingOrderList);
        MatchResult matchResult = orderBook.match(incomingOrder);

        tradeSettlementService.settle(
                matchResult.executedTrades(),
                matchResult.incomingOrder(),
                matchResult.updatedMatchingOrders()
        );

        storeOrderAndTradeRecords(
                matchResult.incomingOrder(),
                matchResult.updatedMatchingOrders(),
                matchResult.executedTrades()
        );

        return PlaceLimitOrderResponse.from(matchResult.incomingOrder());
    }

    private void requireRequest(PlaceLimitOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request is required");
        }
    }

    private List<Order> findMatchingOrderList(Order incomingOrder) {
        return switch (incomingOrder.getSide()) {
            case BUY -> orderRepository.findMatchableSellOrders(
                    incomingOrder.getSymbol(),
                    incomingOrder.getLimitPrice()
            );
            case SELL -> orderRepository.findMatchableBuyOrders(
                    incomingOrder.getSymbol(),
                    incomingOrder.getLimitPrice()
            );
        };
    }

    private void storeOrderAndTradeRecords(
            Order incomingOrder,
            List<Order> matchingOrderList,
            List<Trade> executedTradeList
    ) {
        List<Order> updatedOrderList = new ArrayList<>();
        updatedOrderList.add(incomingOrder);
        updatedOrderList.addAll(findExecutedMatchingOrderList(matchingOrderList, executedTradeList));

        orderRepository.saveAll(updatedOrderList);
        tradeRepository.saveAll(executedTradeList);
    }

    private List<Order> findExecutedMatchingOrderList(
            List<Order> matchingOrderList,
            List<Trade> executedTradeList
    ) {
        Set<UUID> executedOrderIdSet = new HashSet<>();
        for (Trade trade : executedTradeList) {
            executedOrderIdSet.add(trade.getBuyOrderId());
            executedOrderIdSet.add(trade.getSellOrderId());
        }

        return matchingOrderList.stream()
                .filter(order -> executedOrderIdSet.contains(order.getOrderId()))
                .toList();
    }
}

package dev.exchangelab.application;

import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.Settlement;
import dev.exchangelab.domain.model.StockPosition;
import dev.exchangelab.domain.model.Trade;
import dev.exchangelab.domain.model.TraderAccount;
import dev.exchangelab.domain.repository.OrderRepository;
import dev.exchangelab.domain.repository.StockPositionRepository;
import dev.exchangelab.domain.repository.TradeRepository;
import dev.exchangelab.domain.repository.TraderAccountRepository;
import dev.exchangelab.presentation.dto.PlaceLimitOrderRequest;
import dev.exchangelab.presentation.dto.PlaceLimitOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaceLimitOrderUseCaseImpl implements PlaceLimitOrderUseCase {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final TraderAccountRepository traderAccountRepository;
    private final StockPositionRepository stockPositionRepository;

    @Override
    @Transactional
    public PlaceLimitOrderResponse placeLimitOrder(PlaceLimitOrderRequest request) {
        // Stage 1: Receive order
        Order incomingOrder = Order.createLimit(
                request.traderId(),
                request.symbol(),
                request.side(),
                request.limitPrice(),
                request.quantity()
        );

        // Stage 2: Reserve asset
        switch (incomingOrder.getSide()) {
            case BUY -> {
                TraderAccount traderAccount = traderAccountRepository
                        .findForCashReservation(incomingOrder.getTraderId())
                        .orElseThrow(() -> new IllegalStateException("Trader account not found"));

                traderAccount.reserveCash(
                        incomingOrder.getLimitPrice().multiply(incomingOrder.getQuantity())
                );
                traderAccountRepository.save(traderAccount);
            }
            case SELL -> {
                StockPosition stockPosition = stockPositionRepository
                        .findForStockReservation(
                                incomingOrder.getTraderId(),
                                incomingOrder.getSymbol()
                        )
                        .orElseThrow(() -> new IllegalStateException("Trader stock position not found"));

                stockPosition.reserve(incomingOrder.getQuantity());
                stockPositionRepository.save(stockPosition);
            }
        }

        // Stage 3: Match order
        List<Order> matchingOrders = orderRepository.findMatchingOrdersFor(incomingOrder);
        List<Order> updatedMatchingOrders = new ArrayList<>();
        List<Trade> executedTrades = new ArrayList<>();

        for (Order matchingOrder : matchingOrders) {
            if (incomingOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal tradeQuantity = incomingOrder.getRemainingQuantity().min(
                    matchingOrder.getRemainingQuantity()
            );

            executedTrades.add(createTrade(incomingOrder, matchingOrder, tradeQuantity));
            updatedMatchingOrders.add(matchingOrder);

            incomingOrder.fill(tradeQuantity);
            matchingOrder.fill(tradeQuantity);
        }

        // Stage 4: Apply result
        Map<UUID, TraderAccount> accountsByTraderId = new HashMap<>();
        Map<StockPosition.Key, StockPosition> positionsByKey = new HashMap<>();

        for (Trade trade : executedTrades) {
            accountsByTraderId.putIfAbsent(
                    trade.getBuyerTraderId(),
                    traderAccountRepository.findForCashReservation(trade.getBuyerTraderId())
                            .orElseThrow(() -> new IllegalStateException("Buyer account not found"))
            );
            accountsByTraderId.putIfAbsent(
                    trade.getSellerTraderId(),
                    traderAccountRepository.findForCashReservation(trade.getSellerTraderId())
                            .orElseThrow(() -> new IllegalStateException("Seller account not found"))
            );

            StockPosition sellerPosition = stockPositionRepository
                    .findForStockReservation(trade.getSellerTraderId(), trade.getSymbol())
                    .orElseThrow(() -> new IllegalStateException("Seller stock position not found"));
            positionsByKey.putIfAbsent(sellerPosition.key(), sellerPosition);

            StockPosition buyerPosition = stockPositionRepository
                    .findForStockReservation(trade.getBuyerTraderId(), trade.getSymbol())
                    .orElseGet(() -> new StockPosition(
                            UUID.randomUUID(),
                            trade.getBuyerTraderId(),
                            trade.getSymbol(),
                            BigDecimal.ZERO,
                            BigDecimal.ZERO
                    ));
            positionsByKey.putIfAbsent(buyerPosition.key(), buyerPosition);
        }

        Settlement settlement = new Settlement(
                new ArrayList<>(accountsByTraderId.values()),
                new ArrayList<>(positionsByKey.values())
        );
        settlement.settle(incomingOrder, updatedMatchingOrders, executedTrades);
        settlement.accountsToSave().forEach(traderAccountRepository::save);
        settlement.positionsToSave().forEach(stockPositionRepository::save);

        List<Order> ordersToSave = new ArrayList<>();
        ordersToSave.add(incomingOrder);
        ordersToSave.addAll(updatedMatchingOrders);

        orderRepository.saveAll(ordersToSave);
        tradeRepository.saveAll(executedTrades);

        // Stage 5: Return response
        return PlaceLimitOrderResponse.from(incomingOrder);
    }

    private Trade createTrade(
            Order incomingOrder,
            Order matchingOrder,
            BigDecimal tradeQuantity
    ) {
        Order buyOrder = incomingOrder.getSide() == Order.Side.BUY ? incomingOrder : matchingOrder;
        Order sellOrder = incomingOrder.getSide() == Order.Side.SELL ? incomingOrder : matchingOrder;

        return new Trade(
                UUID.randomUUID(),
                buyOrder.getOrderId(),
                sellOrder.getOrderId(),
                buyOrder.getTraderId(),
                sellOrder.getTraderId(),
                incomingOrder.getSymbol(),
                matchingOrder.getLimitPrice(),
                tradeQuantity,
                Instant.now()
        );
    }
}

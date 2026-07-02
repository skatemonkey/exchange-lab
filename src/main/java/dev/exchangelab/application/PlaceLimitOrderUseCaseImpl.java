package dev.exchangelab.application;

import dev.exchangelab.domain.model.Order;
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

            executedTrades.add(Trade.create(incomingOrder, matchingOrder, tradeQuantity));
            updatedMatchingOrders.add(matchingOrder);

            incomingOrder.fill(tradeQuantity);
            matchingOrder.fill(tradeQuantity);
        }

        // Stage 4: Apply result
        Map<UUID, TraderAccount> accountsByTraderId = new HashMap<>();
        Map<StockPosition.Key, StockPosition> positionsByKey = new HashMap<>();
        Map<UUID, Order> ordersById = new HashMap<>();
        ordersById.put(incomingOrder.getOrderId(), incomingOrder);
        updatedMatchingOrders.forEach(order -> ordersById.put(order.getOrderId(), order));

        for (Trade trade : executedTrades) {
            TraderAccount buyerAccount = accountsByTraderId.computeIfAbsent(
                    trade.getBuyerTraderId(),
                    traderId -> traderAccountRepository.findForCashReservation(traderId)
                            .orElseThrow(() -> new IllegalStateException("Buyer account not found"))
            );
            TraderAccount sellerAccount = accountsByTraderId.computeIfAbsent(
                    trade.getSellerTraderId(),
                    traderId -> traderAccountRepository.findForCashReservation(traderId)
                            .orElseThrow(() -> new IllegalStateException("Seller account not found"))
            );

            StockPosition.Key sellerPositionKey = new StockPosition.Key(
                    trade.getSellerTraderId(),
                    trade.getSymbol()
            );
            StockPosition sellerPosition = positionsByKey.computeIfAbsent(
                    sellerPositionKey,
                    key -> stockPositionRepository.findForStockReservation(key.traderId(), key.symbol())
                            .orElseThrow(() -> new IllegalStateException("Seller stock position not found"))
            );

            StockPosition.Key buyerPositionKey = new StockPosition.Key(
                    trade.getBuyerTraderId(),
                    trade.getSymbol()
            );
            StockPosition buyerPosition = positionsByKey.computeIfAbsent(
                    buyerPositionKey,
                    key -> stockPositionRepository.findForStockReservation(key.traderId(), key.symbol())
                            .orElseGet(() -> new StockPosition(
                                    UUID.randomUUID(),
                                    key.traderId(),
                                    key.symbol(),
                                    BigDecimal.ZERO,
                                    BigDecimal.ZERO
                            ))
            );

            Order buyOrder = ordersById.get(trade.getBuyOrderId());
            if (buyOrder == null) {
                throw new IllegalStateException("Buy order not found");
            }

            BigDecimal tradeValue = trade.getPrice().multiply(trade.getQuantity());
            BigDecimal reservedCashToRelease = buyOrder.getLimitPrice().multiply(trade.getQuantity());

            buyerAccount.settleBuy(tradeValue, reservedCashToRelease);
            sellerAccount.receiveCash(tradeValue);

            sellerPosition.settleSell(trade.getQuantity());
            buyerPosition.receive(trade.getQuantity());
        }

        accountsByTraderId.values().forEach(traderAccountRepository::save);
        positionsByKey.values().forEach(stockPositionRepository::save);

        List<Order> ordersToSave = new ArrayList<>();
        ordersToSave.add(incomingOrder);
        ordersToSave.addAll(updatedMatchingOrders);

        orderRepository.saveAll(ordersToSave);
        tradeRepository.saveAll(executedTrades);

        // Stage 5: Return response
        return PlaceLimitOrderResponse.from(incomingOrder);
    }
}

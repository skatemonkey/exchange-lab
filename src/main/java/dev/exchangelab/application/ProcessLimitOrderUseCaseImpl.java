package dev.exchangelab.application;

import dev.exchangelab.common.event.LimitOrderSubmittedEvent;
import dev.exchangelab.application.orderbook.InMemoryOrderBookRegistry;
import dev.exchangelab.domain.model.MatchResult;
import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.StockPosition;
import dev.exchangelab.domain.model.Trade;
import dev.exchangelab.domain.model.TraderAccount;
import dev.exchangelab.domain.repository.OrderRepository;
import dev.exchangelab.domain.repository.StockPositionRepository;
import dev.exchangelab.domain.repository.TradeRepository;
import dev.exchangelab.domain.repository.TraderAccountRepository;
import dev.exchangelab.infrastructure.redis.RedisReservationService;
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
public class ProcessLimitOrderUseCaseImpl implements ProcessLimitOrderUseCase {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final TraderAccountRepository traderAccountRepository;
    private final StockPositionRepository stockPositionRepository;
    private final InMemoryOrderBookRegistry inMemoryOrderBookRegistry;
    private final RedisReservationService redisReservationService;

    @Override
    @Transactional
    public Order process(LimitOrderSubmittedEvent event) {
        // Stage 1: Create order
        Order incomingOrder = Order.createLimit(
                event.orderId(),
                event.traderId(),
                event.symbol(),
                event.side(),
                event.limitPrice(),
                event.quantity(),
                event.submittedAt()
        );

        // Stage 2: Sync Redis reservation into MySQL
        switch (incomingOrder.getSide()) {
            case BUY -> {
                TraderAccount traderAccount = traderAccountRepository
                        .findForCashReservation(incomingOrder.getTraderId())
                        .orElseThrow(() -> new IllegalStateException("Trader account not found"));

                traderAccount.reserveCash(reservedCashFor(event, incomingOrder));
                traderAccountRepository.save(traderAccount);
            }
            case SELL -> {
                StockPosition stockPosition = stockPositionRepository
                        .findForStockReservation(
                                incomingOrder.getTraderId(),
                                incomingOrder.getSymbol()
                        )
                        .orElseThrow(() -> new IllegalStateException("Trader stock position not found"));

                stockPosition.reserve(reservedStockFor(event, incomingOrder));
                stockPositionRepository.save(stockPosition);
            }
        }

        // Stage 3: Match against in-memory order book
        MatchResult matchResult = inMemoryOrderBookRegistry.match(incomingOrder);
        List<Order> updatedMatchingOrders = matchResult.updatedRestingOrders();
        List<Trade> executedTrades = matchResult.trades();

        // Stage 4: Create trades and update orders
        Map<UUID, TraderAccount> accountsByTraderId = new HashMap<>();
        Map<StockPosition.Key, StockPosition> positionsByKey = new HashMap<>();
        Map<UUID, Order> ordersById = new HashMap<>();
        Map<UUID, BigDecimal> availableCashToIncrease = new HashMap<>();
        Map<UUID, BigDecimal> cashAvailableIfMissing = new HashMap<>();
        Map<StockPosition.Key, BigDecimal> availableStockToIncrease = new HashMap<>();
        Map<StockPosition.Key, BigDecimal> stockAvailableIfMissing = new HashMap<>();
        boolean redisReserved = hasRedisReservation(event);
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

            if (redisReserved) {
                cashAvailableIfMissing.putIfAbsent(
                        trade.getSellerTraderId(),
                        sellerAccount.availableCash()
                );
                addAmount(availableCashToIncrease, trade.getSellerTraderId(), tradeValue);

                stockAvailableIfMissing.putIfAbsent(
                        buyerPositionKey,
                        buyerPosition.availableQuantity()
                );
                addAmount(availableStockToIncrease, buyerPositionKey, trade.getQuantity());

                BigDecimal unusedReservedCash = reservedCashToRelease.subtract(tradeValue);
                if (unusedReservedCash.compareTo(BigDecimal.ZERO) > 0) {
                    cashAvailableIfMissing.putIfAbsent(
                            trade.getBuyerTraderId(),
                            buyerAccount.availableCash()
                    );
                    addAmount(availableCashToIncrease, trade.getBuyerTraderId(), unusedReservedCash);
                }
            }

            buyerAccount.settleBuy(tradeValue, reservedCashToRelease);
            sellerAccount.receiveCash(tradeValue);

            sellerPosition.settleSell(trade.getQuantity());
            buyerPosition.receive(trade.getQuantity());
        }

        // Stage 5: Settle cash and stock
        accountsByTraderId.values().forEach(traderAccountRepository::save);
        positionsByKey.values().forEach(stockPositionRepository::save);

        List<Order> ordersToSave = new ArrayList<>();
        ordersToSave.add(incomingOrder);
        ordersToSave.addAll(updatedMatchingOrders);

        orderRepository.saveAll(ordersToSave);
        tradeRepository.saveAll(executedTrades);

        if (redisReserved) {
            syncRedisAvailability(
                    availableCashToIncrease,
                    cashAvailableIfMissing,
                    availableStockToIncrease,
                    stockAvailableIfMissing
            );
        }

        return incomingOrder;
    }

    private BigDecimal reservedCashFor(LimitOrderSubmittedEvent event, Order incomingOrder) {
        if (event.reservedCash() != null) {
            return event.reservedCash();
        }

        return incomingOrder.getLimitPrice().multiply(incomingOrder.getQuantity());
    }

    private BigDecimal reservedStockFor(LimitOrderSubmittedEvent event, Order incomingOrder) {
        if (event.reservedStock() != null) {
            return event.reservedStock();
        }

        return incomingOrder.getQuantity();
    }

    private boolean hasRedisReservation(LimitOrderSubmittedEvent event) {
        return event.reservedCash() != null || event.reservedStock() != null;
    }

    private <T> void addAmount(Map<T, BigDecimal> amounts, T key, BigDecimal amount) {
        amounts.merge(key, amount, BigDecimal::add);
    }

    private void syncRedisAvailability(
            Map<UUID, BigDecimal> availableCashToIncrease,
            Map<UUID, BigDecimal> cashAvailableIfMissing,
            Map<StockPosition.Key, BigDecimal> availableStockToIncrease,
            Map<StockPosition.Key, BigDecimal> stockAvailableIfMissing
    ) {
        availableCashToIncrease.forEach((traderId, amount) ->
                redisReservationService.increaseAvailableCash(
                        traderId,
                        amount,
                        cashAvailableIfMissing.getOrDefault(traderId, BigDecimal.ZERO)
                ));

        availableStockToIncrease.forEach((positionKey, amount) ->
                redisReservationService.increaseAvailableStock(
                        positionKey.traderId(),
                        positionKey.symbol(),
                        amount,
                        stockAvailableIfMissing.getOrDefault(positionKey, BigDecimal.ZERO)
                ));
    }
}

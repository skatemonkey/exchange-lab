package dev.exchangelab.service.impl;

import dev.exchangelab.model.dto.PlaceLimitOrderRequest;
import dev.exchangelab.model.dto.PlaceLimitOrderResponse;
import dev.exchangelab.model.entity.OrderEntity;
import dev.exchangelab.model.entity.StockPositionEntity;
import dev.exchangelab.model.entity.TradeEntity;
import dev.exchangelab.model.entity.TraderAccountEntity;
import dev.exchangelab.model.enums.OrderSide;
import dev.exchangelab.model.enums.OrderStatus;
import dev.exchangelab.repository.dao.OrderDao;
import dev.exchangelab.repository.dao.StockPositionDao;
import dev.exchangelab.repository.dao.TradeDao;
import dev.exchangelab.repository.dao.TraderAccountDao;
import dev.exchangelab.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderDao orderDao;
    private final TraderAccountDao traderAccountDao;
    private final StockPositionDao stockPositionDao;
    private final TradeDao tradeDao;

    @Override
    @Transactional
    public PlaceLimitOrderResponse placeLimitOrder(PlaceLimitOrderRequest request) {
        /*
         * Planned order flow:
         * 1. Validate the limit order request. [done]
         * 2. Validate whether the trader has enough cash or stock. [done]
         * 3. Reserve cash for buy orders or stock quantity for sell orders. [done]
         * 4. Compare the order against the current order book by price-time priority. [done]
         * 5. If matching orders exist, execute one or more trades. [done]
         * 6. Settle each trade by moving cash and stock between traders. [done]
         * 7. Store the order and trade records. [done]
         * 8. Return the final order status to the client. [done]
         */

        // 1. Validate the limit order request. [done]
        validateLimitOrderRequest(request);

        // 2. Validate whether the trader has enough cash or stock. [done]
        validateTraderHasEnoughCashOrStock(request);

        // 3. Reserve cash for buy orders or stock quantity for sell orders. [done]
        reserveCashOrStock(request);

        OrderEntity incomingOrder = createAcceptedOrder(request);

        // 4. Compare the order against the current order book by price-time priority. [done]
        List<OrderEntity> matchingOrderList = findMatchingOrderList(request);

        // 5. If matching orders exist, execute one or more trades. [done]
        List<TradeEntity> executedTradeList = executeTradeList(incomingOrder, matchingOrderList);

        // 6. Settle each trade by moving cash and stock between traders. [done]
        settleTradeList(executedTradeList, incomingOrder, matchingOrderList);

        // 7. Store the order and trade records. [done]
        storeOrderAndTradeRecords(incomingOrder, matchingOrderList, executedTradeList);

        // 8. Return the final order status to the client. [done]
        return PlaceLimitOrderResponse.from(incomingOrder);
    }

    private void validateLimitOrderRequest(PlaceLimitOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request is required");
        }
        if (request.traderId() == null) {
            throw new IllegalArgumentException("Trader id is required");
        }
        if (request.symbol() == null || request.symbol().isBlank()) {
            throw new IllegalArgumentException("Stock symbol is required");
        }
        if (request.side() == null) {
            throw new IllegalArgumentException("Order side is required");
        }
        if (request.limitPrice() == null || request.limitPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Limit price must be greater than zero");
        }
        if (request.quantity() == null || request.quantity().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
    }

    private void validateTraderHasEnoughCashOrStock(PlaceLimitOrderRequest request) {
        switch (request.side()) {
            case BUY -> {
                BigDecimal requiredCash = request.limitPrice().multiply(request.quantity());
                TraderAccountEntity account = traderAccountDao.findAccountForCashCheck(request.traderId())
                        .orElseThrow(() -> new IllegalStateException("Trader account not found"));

                BigDecimal availableCash = account.getCashBalance().subtract(account.getReservedCash());
                if (availableCash.compareTo(requiredCash) < 0) {
                    throw new IllegalStateException("Trader does not have enough available cash");
                }
            }
            case SELL -> {
                BigDecimal requiredQuantity = request.quantity();
                StockPositionEntity position = stockPositionDao.findPositionForStockCheck(
                                request.traderId(),
                                request.symbol()
                        )
                        .orElseThrow(() -> new IllegalStateException("Trader stock position not found"));

                BigDecimal availableQuantity = position.getQuantity().subtract(position.getReservedQuantity());
                if (availableQuantity.compareTo(requiredQuantity) < 0) {
                    throw new IllegalStateException("Trader does not have enough available stock");
                }
            }
        }
    }

    private void reserveCashOrStock(PlaceLimitOrderRequest request) {
        switch (request.side()) {
            case BUY -> {
                BigDecimal reservedCash = request.limitPrice().multiply(request.quantity());
                TraderAccountEntity account = traderAccountDao.findAccountForCashCheck(request.traderId())
                        .orElseThrow(() -> new IllegalStateException("Trader account not found"));

                BigDecimal availableCash = account.getCashBalance().subtract(account.getReservedCash());
                if (availableCash.compareTo(reservedCash) < 0) {
                    throw new IllegalStateException("Cannot reserve more cash than available");
                }

                account.setReservedCash(account.getReservedCash().add(reservedCash));
                traderAccountDao.save(account);
            }
            case SELL -> {
                StockPositionEntity position = stockPositionDao.findPositionForStockCheck(
                                request.traderId(),
                                request.symbol()
                        )
                        .orElseThrow(() -> new IllegalStateException("Trader stock position not found"));

                BigDecimal availableQuantity = position.getQuantity().subtract(position.getReservedQuantity());
                if (availableQuantity.compareTo(request.quantity()) < 0) {
                    throw new IllegalStateException("Cannot reserve more stock than available");
                }

                position.setReservedQuantity(position.getReservedQuantity().add(request.quantity()));
                stockPositionDao.save(position);
            }
        }
    }

    private List<OrderEntity> findMatchingOrderList(PlaceLimitOrderRequest request) {
        return switch (request.side()) {
            case BUY -> orderDao.findMatchableSellOrders(
                    request.symbol(),
                    request.limitPrice()
            );
            case SELL -> orderDao.findMatchableBuyOrders(
                    request.symbol(),
                    request.limitPrice()
            );
        };
    }

    private OrderEntity createAcceptedOrder(PlaceLimitOrderRequest request) {
        return new OrderEntity(
                UUID.randomUUID(),
                request.traderId(),
                request.symbol(),
                request.side(),
                request.limitPrice(),
                request.quantity(),
                request.quantity(),
                OrderStatus.ACCEPTED,
                Instant.now()
        );
    }

    private List<TradeEntity> executeTradeList(
            OrderEntity incomingOrder,
            List<OrderEntity> matchingOrderList
    ) {
        List<TradeEntity> tradeList = new ArrayList<>();

        for (OrderEntity matchingOrder : matchingOrderList) {
            if (incomingOrder.getRemainingQuantity().compareTo(BigDecimal.ZERO) <= 0) {
                break;
            }

            BigDecimal tradeQuantity = incomingOrder.getRemainingQuantity().min(
                    matchingOrder.getRemainingQuantity()
            );

            tradeList.add(createTrade(incomingOrder, matchingOrder, tradeQuantity));
            incomingOrder.setRemainingQuantity(
                    incomingOrder.getRemainingQuantity().subtract(tradeQuantity)
            );
            matchingOrder.setRemainingQuantity(
                    matchingOrder.getRemainingQuantity().subtract(tradeQuantity)
            );

            updateOrderStatus(incomingOrder);
            updateOrderStatus(matchingOrder);
        }

        return tradeList;
    }

    private TradeEntity createTrade(
            OrderEntity incomingOrder,
            OrderEntity matchingOrder,
            BigDecimal tradeQuantity
    ) {
        OrderEntity buyOrder = incomingOrder.getSide() == OrderSide.BUY ? incomingOrder : matchingOrder;
        OrderEntity sellOrder = incomingOrder.getSide() == OrderSide.SELL ? incomingOrder : matchingOrder;

        return new TradeEntity(
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

    private void updateOrderStatus(OrderEntity order) {
        if (order.getRemainingQuantity().compareTo(BigDecimal.ZERO) == 0) {
            order.setStatus(OrderStatus.FILLED);
            return;
        }

        if (order.getRemainingQuantity().compareTo(order.getQuantity()) < 0) {
            order.setStatus(OrderStatus.PARTIALLY_FILLED);
            return;
        }

        order.setStatus(OrderStatus.ACCEPTED);
    }

    private void settleTradeList(
            List<TradeEntity> tradeList,
            OrderEntity incomingOrder,
            List<OrderEntity> matchingOrderList
    ) {
        Map<UUID, OrderEntity> ordersById = new HashMap<>();
        ordersById.put(incomingOrder.getOrderId(), incomingOrder);
        matchingOrderList.forEach(order -> ordersById.put(order.getOrderId(), order));

        for (TradeEntity trade : tradeList) {
            OrderEntity buyOrder = ordersById.get(trade.getBuyOrderId());

            settleCash(trade, buyOrder);
            settleStock(trade);
        }
    }

    private void settleCash(TradeEntity trade, OrderEntity buyOrder) {
        BigDecimal tradeValue = trade.getPrice().multiply(trade.getQuantity());
        BigDecimal reservedCashToRelease = buyOrder.getLimitPrice().multiply(trade.getQuantity());

        TraderAccountEntity buyerAccount = traderAccountDao.findAccountForCashCheck(trade.getBuyerTraderId())
                .orElseThrow(() -> new IllegalStateException("Buyer account not found"));
        TraderAccountEntity sellerAccount = traderAccountDao.findAccountForCashCheck(trade.getSellerTraderId())
                .orElseThrow(() -> new IllegalStateException("Seller account not found"));

        buyerAccount.setCashBalance(buyerAccount.getCashBalance().subtract(tradeValue));
        buyerAccount.setReservedCash(buyerAccount.getReservedCash().subtract(reservedCashToRelease));
        sellerAccount.setCashBalance(sellerAccount.getCashBalance().add(tradeValue));

        traderAccountDao.save(buyerAccount);
        traderAccountDao.save(sellerAccount);
    }

    private void settleStock(TradeEntity trade) {
        StockPositionEntity sellerPosition = stockPositionDao.findPositionForStockCheck(
                        trade.getSellerTraderId(),
                        trade.getSymbol()
                )
                .orElseThrow(() -> new IllegalStateException("Seller stock position not found"));
        StockPositionEntity buyerPosition = stockPositionDao.findPositionForStockCheck(
                        trade.getBuyerTraderId(),
                        trade.getSymbol()
                )
                .orElseGet(() -> new StockPositionEntity(
                        UUID.randomUUID(),
                        trade.getBuyerTraderId(),
                        trade.getSymbol(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ));

        sellerPosition.setQuantity(sellerPosition.getQuantity().subtract(trade.getQuantity()));
        sellerPosition.setReservedQuantity(sellerPosition.getReservedQuantity().subtract(trade.getQuantity()));
        buyerPosition.setQuantity(buyerPosition.getQuantity().add(trade.getQuantity()));

        stockPositionDao.save(sellerPosition);
        stockPositionDao.save(buyerPosition);
    }

    private void storeOrderAndTradeRecords(
            OrderEntity incomingOrder,
            List<OrderEntity> matchingOrderList,
            List<TradeEntity> executedTradeList
    ) {
        List<OrderEntity> updatedOrderList = new ArrayList<>();
        updatedOrderList.add(incomingOrder);
        updatedOrderList.addAll(findExecutedMatchingOrderList(matchingOrderList, executedTradeList));

        orderDao.saveAll(updatedOrderList);
        tradeDao.saveAll(executedTradeList);
    }

    private List<OrderEntity> findExecutedMatchingOrderList(
            List<OrderEntity> matchingOrderList,
            List<TradeEntity> executedTradeList
    ) {
        Set<UUID> executedOrderIdSet = new HashSet<>();
        for (TradeEntity trade : executedTradeList) {
            executedOrderIdSet.add(trade.getBuyOrderId());
            executedOrderIdSet.add(trade.getSellOrderId());
        }

        return matchingOrderList.stream()
                .filter(order -> executedOrderIdSet.contains(order.getOrderId()))
                .toList();
    }
}

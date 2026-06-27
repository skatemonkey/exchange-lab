package dev.exchangelab.service.impl;

import dev.exchangelab.model.dto.PlaceLimitOrderRequest;
import dev.exchangelab.model.dto.PlaceLimitOrderResponse;
import dev.exchangelab.model.entity.OrderEntity;
import dev.exchangelab.model.entity.StockPositionEntity;
import dev.exchangelab.model.entity.TraderAccountEntity;
import dev.exchangelab.repository.dao.OrderDao;
import dev.exchangelab.repository.dao.StockPositionDao;
import dev.exchangelab.repository.dao.TraderAccountDao;
import dev.exchangelab.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderDao orderDao;
    private final TraderAccountDao traderAccountDao;
    private final StockPositionDao stockPositionDao;

    @Override
    public PlaceLimitOrderResponse placeLimitOrder(PlaceLimitOrderRequest request) {
        /*
         * Planned order flow:
         * 1. Validate the limit order request. [done]
         * 2. Validate whether the trader has enough cash or stock. [done]
         * 3. Reserve cash for buy orders or stock quantity for sell orders. [done]
         * 4. Compare the order against the current order book by price-time priority. [done]
         * 5. If matching orders exist, execute one or more trades. [todo]
         * 6. Settle each trade by moving cash and stock between traders. [todo]
         * 7. Store the order, trades, account updates, and position updates. [todo]
         * 8. Return the final order status to the client. [partial]
         */

        // 1. Validate the limit order request. [done]
        validateLimitOrderRequest(request);

        // 2. Validate whether the trader has enough cash or stock. [done]
        validateTraderHasEnoughCashOrStock(request);

        // 3. Reserve cash for buy orders or stock quantity for sell orders. [done]
        reserveCashOrStock(request);

        // 4. Compare the order against the current order book by price-time priority. [done]
        List<OrderEntity> matchingOrders = findMatchingOrders(request);

        // 5. If matching orders exist, execute one or more trades. [todo]

        // 6. Settle each trade by moving cash and stock between traders. [todo]

        // 7. Store the order, trades, account updates, and position updates. [todo]

        // 8. Return the final order status to the client. [partial]
        return PlaceLimitOrderResponse.accepted(
                UUID.randomUUID(),
                request
        );
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

    private List<OrderEntity> findMatchingOrders(PlaceLimitOrderRequest request) {
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
}

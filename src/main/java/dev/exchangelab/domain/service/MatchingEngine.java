package dev.exchangelab.domain.service;

import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.OrderSide;
import dev.exchangelab.domain.model.Trade;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
public class MatchingEngine {

    public MatchResult match(
            Order incomingOrder,
            List<Order> matchingOrders
    ) {
        List<Trade> executedTrades = new ArrayList<>();
        List<Order> updatedMatchingOrders = new ArrayList<>();

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

        return new MatchResult(incomingOrder, updatedMatchingOrders, executedTrades);
    }

    private Trade createTrade(
            Order incomingOrder,
            Order matchingOrder,
            BigDecimal tradeQuantity
    ) {
        Order buyOrder = incomingOrder.getSide() == OrderSide.BUY ? incomingOrder : matchingOrder;
        Order sellOrder = incomingOrder.getSide() == OrderSide.SELL ? incomingOrder : matchingOrder;

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

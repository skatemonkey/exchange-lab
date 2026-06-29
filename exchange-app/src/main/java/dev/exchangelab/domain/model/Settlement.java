package dev.exchangelab.domain.model;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Settlement {

    private final Map<UUID, TraderAccount> accountsByTraderId;
    private final Map<StockPosition.Key, StockPosition> positionsByKey;

    public Settlement(
            List<TraderAccount> accounts,
            List<StockPosition> positions
    ) {
        accountsByTraderId = new HashMap<>();
        for (TraderAccount account : accounts) {
            accountsByTraderId.put(account.getTraderId(), account);
        }

        positionsByKey = new HashMap<>();
        for (StockPosition position : positions) {
            positionsByKey.put(position.key(), position);
        }
    }

    public void settle(MatchResult matchResult) {
        Map<UUID, Order> ordersById = new HashMap<>();
        ordersById.put(matchResult.incomingOrder().getOrderId(), matchResult.incomingOrder());
        matchResult.updatedMatchingOrders().forEach(order -> ordersById.put(order.getOrderId(), order));

        for (Trade trade : matchResult.executedTrades()) {
            Order buyOrder = ordersById.get(trade.getBuyOrderId());

            settleCash(trade, buyOrder);
            settleStock(trade);
        }
    }

    public List<TraderAccount> accountsToSave() {
        return List.copyOf(accountsByTraderId.values());
    }

    public List<StockPosition> positionsToSave() {
        return List.copyOf(positionsByKey.values());
    }

    private void settleCash(Trade trade, Order buyOrder) {
        BigDecimal tradeValue = trade.getPrice().multiply(trade.getQuantity());
        BigDecimal reservedCashToRelease = buyOrder.getLimitPrice().multiply(trade.getQuantity());

        TraderAccount buyerAccount = account(trade.getBuyerTraderId());
        TraderAccount sellerAccount = account(trade.getSellerTraderId());

        buyerAccount.settleBuy(tradeValue, reservedCashToRelease);
        sellerAccount.receiveCash(tradeValue);
    }

    private void settleStock(Trade trade) {
        StockPosition sellerPosition = position(trade.getSellerTraderId(), trade.getSymbol());
        StockPosition buyerPosition = position(trade.getBuyerTraderId(), trade.getSymbol());

        sellerPosition.settleSell(trade.getQuantity());
        buyerPosition.receive(trade.getQuantity());
    }

    private TraderAccount account(UUID traderId) {
        TraderAccount account = accountsByTraderId.get(traderId);
        if (account == null) {
            throw new IllegalStateException("Trader account not loaded");
        }
        return account;
    }

    private StockPosition position(UUID traderId, String symbol) {
        StockPosition position = positionsByKey.get(new StockPosition.Key(traderId, symbol));
        if (position == null) {
            throw new IllegalStateException("Stock position not loaded");
        }
        return position;
    }
}

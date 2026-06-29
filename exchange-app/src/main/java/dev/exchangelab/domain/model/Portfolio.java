package dev.exchangelab.domain.model;

import java.math.BigDecimal;
import java.util.Optional;

public class Portfolio {

    private final TraderAccount traderAccount;
    private final StockPosition stockPosition;

    private Portfolio(TraderAccount traderAccount, StockPosition stockPosition) {
        this.traderAccount = traderAccount;
        this.stockPosition = stockPosition;
    }

    public static Portfolio cash(TraderAccount traderAccount) {
        return new Portfolio(traderAccount, null);
    }

    public static Portfolio stock(StockPosition stockPosition) {
        return new Portfolio(null, stockPosition);
    }

    public void reserveFor(Order order) {
        switch (order.getSide()) {
            case BUY -> reserveCash(order.getLimitPrice(), order.getQuantity());
            case SELL -> reserveStock(order.getQuantity());
        }
    }

    public Optional<TraderAccount> traderAccountToSave() {
        return Optional.ofNullable(traderAccount);
    }

    public Optional<StockPosition> stockPositionToSave() {
        return Optional.ofNullable(stockPosition);
    }

    private void reserveCash(BigDecimal limitPrice, BigDecimal quantity) {
        if (traderAccount == null) {
            throw new IllegalStateException("Trader account not loaded");
        }

        traderAccount.reserveCash(limitPrice.multiply(quantity));
    }

    private void reserveStock(BigDecimal quantity) {
        if (stockPosition == null) {
            throw new IllegalStateException("Trader stock position not loaded");
        }

        stockPosition.reserve(quantity);
    }
}

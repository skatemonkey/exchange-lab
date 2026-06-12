package dev.exchangelab.market;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class Trade {

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    private final UUID tradeId;
    private final UUID buyOrderId;
    private final UUID sellOrderId;
    private final UUID buyerTraderId;
    private final UUID sellerTraderId;
    private final String stockSymbol;
    private final BigDecimal price;
    private final BigDecimal quantity;
    private final Instant executedAt;

    // ---------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------

    public Trade(
            UUID tradeId,
            UUID buyOrderId,
            UUID sellOrderId,
            UUID buyerTraderId,
            UUID sellerTraderId,
            String stockSymbol,
            BigDecimal price,
            BigDecimal quantity,
            Instant executedAt
    ) {
        if (price == null || price.signum() <= 0) {
            throw new IllegalArgumentException("Trade price must be greater than zero");
        }
        if (quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("Trade quantity must be greater than zero");
        }

        this.tradeId = Objects.requireNonNull(tradeId);
        this.buyOrderId = Objects.requireNonNull(buyOrderId);
        this.sellOrderId = Objects.requireNonNull(sellOrderId);
        this.buyerTraderId = Objects.requireNonNull(buyerTraderId);
        this.sellerTraderId = Objects.requireNonNull(sellerTraderId);
        this.stockSymbol = Objects.requireNonNull(stockSymbol);
        this.price = price;
        this.quantity = quantity;
        this.executedAt = Objects.requireNonNull(executedAt);
    }
}

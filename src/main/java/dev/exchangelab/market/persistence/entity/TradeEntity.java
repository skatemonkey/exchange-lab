package dev.exchangelab.market.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "trades")
public class TradeEntity {

    @Id
    @Column(name = "trade_id", nullable = false)
    private UUID tradeId;

    @Column(name = "buy_order_id", nullable = false)
    private UUID buyOrderId;

    @Column(name = "sell_order_id", nullable = false)
    private UUID sellOrderId;

    @Column(name = "buyer_trader_id", nullable = false)
    private UUID buyerTraderId;

    @Column(name = "seller_trader_id", nullable = false)
    private UUID sellerTraderId;

    @Column(name = "stock_symbol", nullable = false, length = 20)
    private String stockSymbol;

    @Column(name = "price", nullable = false, precision = 19, scale = 8)
    private BigDecimal price;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    @Column(name = "executed_at", nullable = false)
    private Instant executedAt;

    protected TradeEntity() {
    }

    public TradeEntity(
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
        this.tradeId = tradeId;
        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.buyerTraderId = buyerTraderId;
        this.sellerTraderId = sellerTraderId;
        this.stockSymbol = stockSymbol;
        this.price = price;
        this.quantity = quantity;
        this.executedAt = executedAt;
    }
}

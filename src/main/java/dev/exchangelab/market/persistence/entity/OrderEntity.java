package dev.exchangelab.market.persistence.entity;

import dev.exchangelab.market.domain.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "trader_id", nullable = false)
    private UUID traderId;

    @Column(name = "stock_symbol", nullable = false, length = 20)
    private String stockSymbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false, length = 10)
    private Order.Side side;

    @Column(name = "limit_price", nullable = false, precision = 19, scale = 8)
    private BigDecimal limitPrice;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    @Column(name = "remaining_quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal remainingQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Order.Status status;

    @Column(name = "submitted_at", nullable = false)
    private Instant submittedAt;

    protected OrderEntity() {
    }

    public OrderEntity(
            UUID orderId,
            UUID traderId,
            String stockSymbol,
            Order.Side side,
            BigDecimal limitPrice,
            BigDecimal quantity,
            BigDecimal remainingQuantity,
            Order.Status status,
            Instant submittedAt
    ) {
        this.orderId = orderId;
        this.traderId = traderId;
        this.stockSymbol = stockSymbol;
        this.side = side;
        this.limitPrice = limitPrice;
        this.quantity = quantity;
        this.remainingQuantity = remainingQuantity;
        this.status = status;
        this.submittedAt = submittedAt;
    }
}

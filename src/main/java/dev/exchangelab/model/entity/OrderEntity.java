package dev.exchangelab.model.entity;

import dev.exchangelab.model.enums.OrderSide;
import dev.exchangelab.model.enums.OrderStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "orders")
public class OrderEntity {

    @Id
    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "trader_id", nullable = false)
    private UUID traderId;

    @Column(name = "symbol", nullable = false, length = 20)
    private String symbol;

    @Enumerated(EnumType.STRING)
    @Column(name = "side", nullable = false, length = 10)
    private OrderSide side;

    @Column(name = "limit_price", nullable = false, precision = 19, scale = 8)
    private BigDecimal limitPrice;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    @Column(name = "remaining_quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal remainingQuantity;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected OrderEntity() {
    }

    public OrderEntity(
            UUID orderId,
            UUID traderId,
            String symbol,
            OrderSide side,
            BigDecimal limitPrice,
            BigDecimal quantity,
            BigDecimal remainingQuantity,
            OrderStatus status,
            Instant createdAt
    ) {
        this.orderId = orderId;
        this.traderId = traderId;
        this.symbol = symbol;
        this.side = side;
        this.limitPrice = limitPrice;
        this.quantity = quantity;
        this.remainingQuantity = remainingQuantity;
        this.status = status;
        this.createdAt = createdAt;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public UUID getTraderId() {
        return traderId;
    }

    public String getSymbol() {
        return symbol;
    }

    public OrderSide getSide() {
        return side;
    }

    public BigDecimal getLimitPrice() {
        return limitPrice;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public BigDecimal getRemainingQuantity() {
        return remainingQuantity;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

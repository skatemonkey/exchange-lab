package dev.exchangelab.infrastructure.persistence.entity;

import dev.exchangelab.domain.model.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Entity
@Table(name = "orders")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
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
    private Order.Side side;

    @Column(name = "limit_price", nullable = false, precision = 19, scale = 8)
    private BigDecimal limitPrice;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    @Setter
    @Column(name = "remaining_quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal remainingQuantity;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Order.Status status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

}

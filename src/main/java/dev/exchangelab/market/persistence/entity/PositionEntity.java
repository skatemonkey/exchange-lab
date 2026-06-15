package dev.exchangelab.market.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Getter
@Entity
@Table(name = "positions")
public class PositionEntity {

    @EmbeddedId
    private PositionId id;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal quantity;

    @Column(name = "reserved_quantity", nullable = false, precision = 19, scale = 8)
    private BigDecimal reservedQuantity;

    protected PositionEntity() {
    }

    public PositionEntity(UUID traderId, String stockSymbol, BigDecimal quantity, BigDecimal reservedQuantity) {
        this.id = new PositionId(traderId, stockSymbol);
        this.quantity = quantity;
        this.reservedQuantity = reservedQuantity;
    }

    @Getter
    @Embeddable
    public static class PositionId implements Serializable {

        @Column(name = "trader_id", nullable = false)
        private UUID traderId;

        @Column(name = "stock_symbol", nullable = false, length = 20)
        private String stockSymbol;

        protected PositionId() {
        }

        public PositionId(UUID traderId, String stockSymbol) {
            this.traderId = traderId;
            this.stockSymbol = stockSymbol;
        }

        @Override
        public boolean equals(Object other) {
            if (this == other) {
                return true;
            }
            if (!(other instanceof PositionId that)) {
                return false;
            }
            return Objects.equals(traderId, that.traderId)
                    && Objects.equals(stockSymbol, that.stockSymbol);
        }

        @Override
        public int hashCode() {
            return Objects.hash(traderId, stockSymbol);
        }
    }
}

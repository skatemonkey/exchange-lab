package dev.exchangelab.market.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Entity
@Table(name = "trader_accounts")
public class TraderAccountEntity {

    @Id
    @Column(name = "trader_id", nullable = false)
    private UUID traderId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "cash_balance", nullable = false, precision = 19, scale = 8)
    private BigDecimal cashBalance;

    @Column(name = "reserved_cash", nullable = false, precision = 19, scale = 8)
    private BigDecimal reservedCash;

    protected TraderAccountEntity() {
    }

    public TraderAccountEntity(UUID traderId, String username, BigDecimal cashBalance, BigDecimal reservedCash) {
        this.traderId = traderId;
        this.username = username;
        this.cashBalance = cashBalance;
        this.reservedCash = reservedCash;
    }
}

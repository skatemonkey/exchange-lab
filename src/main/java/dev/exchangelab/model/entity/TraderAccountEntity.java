package dev.exchangelab.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "trader_accounts")
public class TraderAccountEntity {

    @Id
    @Column(name = "trader_id", nullable = false)
    private UUID traderId;

    @Column(name = "cash_balance", nullable = false, precision = 19, scale = 8)
    private BigDecimal cashBalance;

    @Column(name = "reserved_cash", nullable = false, precision = 19, scale = 8)
    private BigDecimal reservedCash;

    protected TraderAccountEntity() {
    }

    public TraderAccountEntity(
            UUID traderId,
            BigDecimal cashBalance,
            BigDecimal reservedCash
    ) {
        this.traderId = traderId;
        this.cashBalance = cashBalance;
        this.reservedCash = reservedCash;
    }

    public UUID getTraderId() {
        return traderId;
    }

    public BigDecimal getCashBalance() {
        return cashBalance;
    }

    public BigDecimal getReservedCash() {
        return reservedCash;
    }

    public BigDecimal getAvailableCash() {
        return cashBalance.subtract(reservedCash);
    }
}

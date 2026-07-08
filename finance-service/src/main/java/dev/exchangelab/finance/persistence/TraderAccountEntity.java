package dev.exchangelab.finance.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Entity
@Table(name = "trader_accounts")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TraderAccountEntity {

    @Id
    @Column(name = "trader_id", nullable = false)
    private UUID traderId;

    @Column(name = "cash_balance", nullable = false, precision = 19, scale = 8)
    private BigDecimal cashBalance;

    @Column(name = "reserved_cash", nullable = false, precision = 19, scale = 8)
    private BigDecimal reservedCash;

    public BigDecimal availableCash() {
        return cashBalance.subtract(reservedCash);
    }

    public void reserveCash(BigDecimal amount) {
        validatePositive(amount);
        if (availableCash().compareTo(amount) < 0) {
            throw new IllegalStateException("Trader does not have enough available cash");
        }

        reservedCash = reservedCash.add(amount);
    }

    public void settleBuy(BigDecimal tradeValue, BigDecimal reservedCashToRelease) {
        validatePositive(tradeValue);
        validatePositive(reservedCashToRelease);
        if (reservedCash.compareTo(reservedCashToRelease) < 0) {
            throw new IllegalStateException("Cannot release more reserved cash than available");
        }
        if (cashBalance.compareTo(tradeValue) < 0) {
            throw new IllegalStateException("Trader does not have enough cash to settle trade");
        }

        cashBalance = cashBalance.subtract(tradeValue);
        reservedCash = reservedCash.subtract(reservedCashToRelease);
    }

    public void receiveCash(BigDecimal amount) {
        validatePositive(amount);
        cashBalance = cashBalance.add(amount);
    }

    private void validatePositive(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}

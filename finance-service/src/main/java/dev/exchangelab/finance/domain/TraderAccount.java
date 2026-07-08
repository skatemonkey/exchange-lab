package dev.exchangelab.finance.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class TraderAccount {

    private final UUID traderId;
    private BigDecimal cashBalance;
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

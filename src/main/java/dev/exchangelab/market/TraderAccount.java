package dev.exchangelab.market;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Getter
public class TraderAccount {

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    private final UUID traderId;
    private final String username;
    private final BigDecimal cashBalance;
    private final BigDecimal reservedCash;

    // ---------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------

    public TraderAccount(
            UUID traderId,
            String username,
            BigDecimal cashBalance,
            BigDecimal reservedCash
    ) {
        if (cashBalance == null || cashBalance.signum() < 0) {
            throw new IllegalArgumentException("Cash balance cannot be negative");
        }
        if (reservedCash == null || reservedCash.signum() < 0) {
            throw new IllegalArgumentException("Reserved cash cannot be negative");
        }
        if (reservedCash.compareTo(cashBalance) > 0) {
            throw new IllegalArgumentException("Reserved cash cannot exceed cash balance");
        }

        this.traderId = Objects.requireNonNull(traderId);
        this.username = requireText(username, "Username is required");
        this.cashBalance = cashBalance;
        this.reservedCash = reservedCash;
    }

    // ---------------------------------------------------------------------
    // Derived State
    // ---------------------------------------------------------------------

    public BigDecimal getAvailableCash() {
        return cashBalance.subtract(reservedCash);
    }

    // ---------------------------------------------------------------------
    // Validation
    // ---------------------------------------------------------------------

    private static String requireText(String value, String message) {
        Objects.requireNonNull(value, message);

        String trimmed = value.trim();
        if (trimmed.isEmpty()) {
            throw new IllegalArgumentException(message);
        }

        return trimmed;
    }
}

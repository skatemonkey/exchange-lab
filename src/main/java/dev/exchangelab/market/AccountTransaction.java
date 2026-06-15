package dev.exchangelab.market;

import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

@Getter
public class AccountTransaction {

    // ---------------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------------

    private final UUID transactionId;
    private final UUID traderId;
    private final Type type;
    private final BigDecimal amount;
    private final BigDecimal balanceAfter;
    private final UUID relatedTradeId;
    private final Instant createdAt;

    // ---------------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------------

    public AccountTransaction(
            UUID transactionId,
            UUID traderId,
            Type type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            UUID relatedTradeId,
            Instant createdAt
    ) {
        if (amount == null || amount.signum() == 0) {
            throw new IllegalArgumentException("Transaction amount cannot be zero");
        }
        if (balanceAfter == null || balanceAfter.signum() < 0) {
            throw new IllegalArgumentException("Balance after transaction cannot be negative");
        }

        this.transactionId = Objects.requireNonNull(transactionId);
        this.traderId = Objects.requireNonNull(traderId);
        this.type = Objects.requireNonNull(type);
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.relatedTradeId = relatedTradeId;
        this.createdAt = Objects.requireNonNull(createdAt);
    }

    // ---------------------------------------------------------------------
    // Types
    // ---------------------------------------------------------------------

    public enum Type {
        INITIAL_CASH,
        DEPOSIT,
        WITHDRAWAL,
        TRADE_DEBIT,
        TRADE_CREDIT,
        CASH_RESERVED,
        CASH_RELEASED
    }
}

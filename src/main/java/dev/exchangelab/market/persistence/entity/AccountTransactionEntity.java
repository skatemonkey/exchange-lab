package dev.exchangelab.market.persistence.entity;

import dev.exchangelab.market.AccountTransaction;
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
@Table(name = "account_transactions")
public class AccountTransactionEntity {

    @Id
    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "trader_id", nullable = false)
    private UUID traderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 30)
    private AccountTransaction.Type type;

    @Column(name = "amount", nullable = false, precision = 19, scale = 8)
    private BigDecimal amount;

    @Column(name = "balance_after", nullable = false, precision = 19, scale = 8)
    private BigDecimal balanceAfter;

    @Column(name = "related_trade_id")
    private UUID relatedTradeId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected AccountTransactionEntity() {
    }

    public AccountTransactionEntity(
            UUID transactionId,
            UUID traderId,
            AccountTransaction.Type type,
            BigDecimal amount,
            BigDecimal balanceAfter,
            UUID relatedTradeId,
            Instant createdAt
    ) {
        this.transactionId = transactionId;
        this.traderId = traderId;
        this.type = type;
        this.amount = amount;
        this.balanceAfter = balanceAfter;
        this.relatedTradeId = relatedTradeId;
        this.createdAt = createdAt;
    }
}

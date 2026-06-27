package dev.exchangelab.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    @Setter
    @Column(name = "reserved_cash", nullable = false, precision = 19, scale = 8)
    private BigDecimal reservedCash;
}

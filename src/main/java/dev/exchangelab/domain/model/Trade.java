package dev.exchangelab.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class Trade {

    private final UUID tradeId;
    private final UUID buyOrderId;
    private final UUID sellOrderId;
    private final UUID buyerTraderId;
    private final UUID sellerTraderId;
    private final String symbol;
    private final BigDecimal price;
    private final BigDecimal quantity;
    private final Instant createdAt;
}

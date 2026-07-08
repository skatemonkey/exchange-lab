package dev.exchangelab.common.finance;

import java.math.BigDecimal;
import java.util.UUID;

public record ReserveStockRequest(
        UUID traderId,
        String symbol,
        BigDecimal amount
) {
}

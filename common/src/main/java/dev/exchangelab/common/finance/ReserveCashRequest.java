package dev.exchangelab.common.finance;

import java.math.BigDecimal;
import java.util.UUID;

public record ReserveCashRequest(
        UUID traderId,
        BigDecimal amount
) {
}

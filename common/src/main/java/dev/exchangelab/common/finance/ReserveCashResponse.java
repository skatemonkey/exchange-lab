package dev.exchangelab.common.finance;

import java.math.BigDecimal;

public record ReserveCashResponse(
        BigDecimal reservedCash
) {
}

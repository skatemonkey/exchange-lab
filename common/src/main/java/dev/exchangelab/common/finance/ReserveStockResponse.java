package dev.exchangelab.common.finance;

import java.math.BigDecimal;

public record ReserveStockResponse(
        BigDecimal reservedStock
) {
}

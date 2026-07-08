package dev.exchangelab.exchange.finance;

import dev.exchangelab.common.finance.ReserveCashRequest;
import dev.exchangelab.common.finance.ReserveCashResponse;
import dev.exchangelab.common.finance.ReserveStockRequest;
import dev.exchangelab.common.finance.ReserveStockResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "finance-service", url = "${services.finance.url}")
public interface FinanceClient {

    @PostMapping("/api/finance/reservations/cash")
    ReserveCashResponse reserveCash(@RequestBody ReserveCashRequest request);

    @PostMapping("/api/finance/reservations/stock")
    ReserveStockResponse reserveStock(@RequestBody ReserveStockRequest request);
}

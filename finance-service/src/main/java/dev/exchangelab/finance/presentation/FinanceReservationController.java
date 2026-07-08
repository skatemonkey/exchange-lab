package dev.exchangelab.finance.presentation;

import dev.exchangelab.common.finance.ReserveCashRequest;
import dev.exchangelab.common.finance.ReserveCashResponse;
import dev.exchangelab.common.finance.ReserveStockRequest;
import dev.exchangelab.common.finance.ReserveStockResponse;
import dev.exchangelab.finance.application.FinanceReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/finance/reservations")
@RequiredArgsConstructor
public class FinanceReservationController {

    private final FinanceReservationService financeReservationService;

    @PostMapping("/cash")
    public ReserveCashResponse reserveCash(@RequestBody ReserveCashRequest request) {
        return financeReservationService.reserveCash(request);
    }

    @PostMapping("/stock")
    public ReserveStockResponse reserveStock(@RequestBody ReserveStockRequest request) {
        return financeReservationService.reserveStock(request);
    }
}

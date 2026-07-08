package dev.exchangelab.finance.application;

import dev.exchangelab.common.finance.ReserveCashRequest;
import dev.exchangelab.common.finance.ReserveCashResponse;
import dev.exchangelab.common.finance.ReserveStockRequest;
import dev.exchangelab.common.finance.ReserveStockResponse;
import dev.exchangelab.finance.persistence.StockPositionEntity;
import dev.exchangelab.finance.persistence.StockPositionRepository;
import dev.exchangelab.finance.persistence.TraderAccountEntity;
import dev.exchangelab.finance.persistence.TraderAccountRepository;
import dev.exchangelab.finance.redis.RedisReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinanceReservationService {

    private final RedisReservationService redisReservationService;
    private final TraderAccountRepository traderAccountRepository;
    private final StockPositionRepository stockPositionRepository;

    public ReserveCashResponse reserveCash(ReserveCashRequest request) {
        if (!redisReservationService.reserveCashIfLoaded(request.traderId(), request.amount())) {
            TraderAccountEntity account = traderAccountRepository.findById(request.traderId())
                    .orElseThrow(() -> new IllegalStateException("Trader account not found"));
            redisReservationService.reserveCash(
                    request.traderId(),
                    request.amount(),
                    account.availableCash()
            );
        }

        return new ReserveCashResponse(request.amount());
    }

    public ReserveStockResponse reserveStock(ReserveStockRequest request) {
        if (!redisReservationService.reserveStockIfLoaded(
                request.traderId(),
                request.symbol(),
                request.amount()
        )) {
            StockPositionEntity position = stockPositionRepository
                    .findByTraderIdAndSymbol(request.traderId(), request.symbol())
                    .orElseThrow(() -> new IllegalStateException("Trader stock position not found"));
            redisReservationService.reserveStock(
                    request.traderId(),
                    request.symbol(),
                    request.amount(),
                    position.availableQuantity()
            );
        }

        return new ReserveStockResponse(request.amount());
    }
}

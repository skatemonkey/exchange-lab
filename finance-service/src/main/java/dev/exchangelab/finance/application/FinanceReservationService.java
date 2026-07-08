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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FinanceReservationService {

    private final RedisReservationService redisReservationService;
    private final TraderAccountRepository traderAccountRepository;
    private final StockPositionRepository stockPositionRepository;

    @Transactional
    public ReserveCashResponse reserveCash(ReserveCashRequest request) {
        TraderAccountEntity account = traderAccountRepository.findByIdForUpdate(request.traderId())
                .orElseThrow(() -> new IllegalStateException("Trader account not found"));

        if (!redisReservationService.reserveCashIfLoaded(request.traderId(), request.amount())) {
            redisReservationService.reserveCash(
                    request.traderId(),
                    request.amount(),
                    account.availableCash()
            );
        }

        account.reserveCash(request.amount());
        traderAccountRepository.save(account);

        return new ReserveCashResponse(request.amount());
    }

    @Transactional
    public ReserveStockResponse reserveStock(ReserveStockRequest request) {
        StockPositionEntity position = stockPositionRepository
                .findByTraderIdAndSymbolForUpdate(request.traderId(), request.symbol())
                .orElseThrow(() -> new IllegalStateException("Trader stock position not found"));

        if (!redisReservationService.reserveStockIfLoaded(
                request.traderId(),
                request.symbol(),
                request.amount()
        )) {
            redisReservationService.reserveStock(
                    request.traderId(),
                    request.symbol(),
                    request.amount(),
                    position.availableQuantity()
            );
        }

        position.reserve(request.amount());
        stockPositionRepository.save(position);

        return new ReserveStockResponse(request.amount());
    }
}

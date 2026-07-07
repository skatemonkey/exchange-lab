package dev.exchangelab.application;

import dev.exchangelab.application.event.LimitOrderSubmittedEvent;
import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.StockPosition;
import dev.exchangelab.domain.model.TraderAccount;
import dev.exchangelab.domain.repository.StockPositionRepository;
import dev.exchangelab.domain.repository.TraderAccountRepository;
import dev.exchangelab.infrastructure.redis.RedisReservationService;
import dev.exchangelab.presentation.dto.PlaceLimitOrderRequest;
import dev.exchangelab.presentation.dto.PlaceLimitOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PlaceLimitOrderUseCaseImpl implements PlaceLimitOrderUseCase {

    private final OrderEventPublisher orderEventPublisher;
    private final RedisReservationService redisReservationService;
    private final TraderAccountRepository traderAccountRepository;
    private final StockPositionRepository stockPositionRepository;

    @Override
    public PlaceLimitOrderResponse placeLimitOrder(PlaceLimitOrderRequest request) {
        Order incomingOrder = Order.createLimit(
                request.traderId(),
                request.symbol(),
                request.side(),
                request.limitPrice(),
                request.quantity()
        );

        BigDecimal reservedCash = null;
        BigDecimal reservedStock = null;

        switch (incomingOrder.getSide()) {
            case BUY -> {
                reservedCash = incomingOrder.getLimitPrice().multiply(incomingOrder.getQuantity());
                loadAvailableCashIfMissing(incomingOrder);
                redisReservationService.reserveCash(incomingOrder.getTraderId(), reservedCash);
            }
            case SELL -> {
                reservedStock = incomingOrder.getQuantity();
                loadAvailableStockIfMissing(incomingOrder);
                redisReservationService.reserveStock(
                        incomingOrder.getTraderId(),
                        incomingOrder.getSymbol(),
                        reservedStock
                );
            }
        }

        orderEventPublisher.publish(LimitOrderSubmittedEvent.from(
                incomingOrder,
                reservedCash,
                reservedStock
        ));
        return PlaceLimitOrderResponse.from(incomingOrder);
    }

    private void loadAvailableCashIfMissing(Order incomingOrder) {
        if (redisReservationService.findAvailableCash(incomingOrder.getTraderId()).isPresent()) {
            return;
        }

        TraderAccount traderAccount = traderAccountRepository
                .findForCashReservation(incomingOrder.getTraderId())
                .orElseThrow(() -> new IllegalStateException("Trader account not found"));

        redisReservationService.setAvailableCash(
                incomingOrder.getTraderId(),
                traderAccount.availableCash()
        );
    }

    private void loadAvailableStockIfMissing(Order incomingOrder) {
        if (redisReservationService
                .findAvailableStock(incomingOrder.getTraderId(), incomingOrder.getSymbol())
                .isPresent()) {
            return;
        }

        StockPosition stockPosition = stockPositionRepository
                .findForStockReservation(
                        incomingOrder.getTraderId(),
                        incomingOrder.getSymbol()
                )
                .orElseThrow(() -> new IllegalStateException("Trader stock position not found"));

        redisReservationService.setAvailableStock(
                incomingOrder.getTraderId(),
                incomingOrder.getSymbol(),
                stockPosition.availableQuantity()
        );
    }
}

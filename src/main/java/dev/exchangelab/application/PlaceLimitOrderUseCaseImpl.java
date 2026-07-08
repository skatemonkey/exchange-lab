package dev.exchangelab.application;

import dev.exchangelab.common.dto.PlaceLimitOrderRequest;
import dev.exchangelab.common.dto.PlaceLimitOrderResponse;
import dev.exchangelab.common.event.LimitOrderSubmittedEvent;
import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.StockPosition;
import dev.exchangelab.domain.model.TraderAccount;
import dev.exchangelab.domain.repository.StockPositionRepository;
import dev.exchangelab.domain.repository.TraderAccountRepository;
import dev.exchangelab.infrastructure.redis.RedisReservationService;
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
                reserveCash(incomingOrder, reservedCash);
            }
            case SELL -> {
                reservedStock = incomingOrder.getQuantity();
                reserveStock(incomingOrder, reservedStock);
            }
        }

        orderEventPublisher.publish(toEvent(incomingOrder, reservedCash, reservedStock));
        return toResponse(incomingOrder);
    }

    private void reserveCash(Order incomingOrder, BigDecimal reservedCash) {
        if (redisReservationService.reserveCashIfLoaded(incomingOrder.getTraderId(), reservedCash)) {
            return;
        }

        TraderAccount traderAccount = traderAccountRepository
                .findForCashReservation(incomingOrder.getTraderId())
                .orElseThrow(() -> new IllegalStateException("Trader account not found"));

        redisReservationService.reserveCash(
                incomingOrder.getTraderId(),
                reservedCash,
                traderAccount.availableCash()
        );
    }

    private void reserveStock(Order incomingOrder, BigDecimal reservedStock) {
        if (redisReservationService.reserveStockIfLoaded(
                incomingOrder.getTraderId(),
                incomingOrder.getSymbol(),
                reservedStock
        )) {
            return;
        }

        StockPosition stockPosition = stockPositionRepository
                .findForStockReservation(
                        incomingOrder.getTraderId(),
                        incomingOrder.getSymbol()
                )
                .orElseThrow(() -> new IllegalStateException("Trader stock position not found"));

        redisReservationService.reserveStock(
                incomingOrder.getTraderId(),
                incomingOrder.getSymbol(),
                reservedStock,
                stockPosition.availableQuantity()
        );
    }

    private LimitOrderSubmittedEvent toEvent(
            Order order,
            BigDecimal reservedCash,
            BigDecimal reservedStock
    ) {
        return new LimitOrderSubmittedEvent(
                order.getOrderId(),
                order.getTraderId(),
                order.getSymbol(),
                order.getSide(),
                order.getLimitPrice(),
                order.getQuantity(),
                reservedCash,
                reservedStock,
                order.getCreatedAt()
        );
    }

    private PlaceLimitOrderResponse toResponse(Order order) {
        return new PlaceLimitOrderResponse(
                order.getOrderId(),
                order.getTraderId(),
                order.getSymbol(),
                order.getSide(),
                order.getLimitPrice(),
                order.getQuantity(),
                order.getStatus()
        );
    }
}

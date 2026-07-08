package dev.exchangelab.exchange.application;

import dev.exchangelab.common.dto.PlaceLimitOrderRequest;
import dev.exchangelab.common.dto.PlaceLimitOrderResponse;
import dev.exchangelab.common.event.LimitOrderSubmittedEvent;
import dev.exchangelab.common.finance.ReserveCashRequest;
import dev.exchangelab.common.finance.ReserveCashResponse;
import dev.exchangelab.common.finance.ReserveStockRequest;
import dev.exchangelab.common.finance.ReserveStockResponse;
import dev.exchangelab.common.order.OrderStatus;
import dev.exchangelab.exchange.finance.FinanceClient;
import dev.exchangelab.exchange.kafka.OrderEventPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaceLimitOrderService {

    private final FinanceClient financeClient;
    private final OrderEventPublisher orderEventPublisher;

    public PlaceLimitOrderResponse placeLimitOrder(PlaceLimitOrderRequest request) {
        UUID orderId = UUID.randomUUID();
        Instant submittedAt = Instant.now();
        BigDecimal reservedCash = null;
        BigDecimal reservedStock = null;

        switch (request.side()) {
            case BUY -> {
                BigDecimal cashToReserve = request.limitPrice().multiply(request.quantity());
                ReserveCashResponse response = financeClient.reserveCash(new ReserveCashRequest(
                        request.traderId(),
                        cashToReserve
                ));
                reservedCash = response.reservedCash();
            }
            case SELL -> {
                ReserveStockResponse response = financeClient.reserveStock(new ReserveStockRequest(
                        request.traderId(),
                        request.symbol(),
                        request.quantity()
                ));
                reservedStock = response.reservedStock();
            }
        }

        orderEventPublisher.publish(new LimitOrderSubmittedEvent(
                orderId,
                request.traderId(),
                request.symbol(),
                request.side(),
                request.limitPrice(),
                request.quantity(),
                reservedCash,
                reservedStock,
                submittedAt
        ));

        return new PlaceLimitOrderResponse(
                orderId,
                request.traderId(),
                request.symbol(),
                request.side(),
                request.limitPrice(),
                request.quantity(),
                OrderStatus.ACCEPTED
        );
    }
}

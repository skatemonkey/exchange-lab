package dev.exchangelab.exchange.application;

import dev.exchangelab.common.dto.PlaceLimitOrderRequest;
import dev.exchangelab.common.event.LimitOrderSubmittedEvent;
import dev.exchangelab.common.finance.ReserveCashRequest;
import dev.exchangelab.common.finance.ReserveCashResponse;
import dev.exchangelab.common.finance.ReserveStockRequest;
import dev.exchangelab.common.finance.ReserveStockResponse;
import dev.exchangelab.common.order.OrderSide;
import dev.exchangelab.common.order.OrderStatus;
import dev.exchangelab.exchange.finance.FinanceClient;
import dev.exchangelab.exchange.kafka.OrderEventPublisher;
import dev.exchangelab.exchange.metrics.ExchangeMetrics;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PlaceLimitOrderServiceTest {

    private static final String SYMBOL = "ACME";

    @Mock
    private FinanceClient financeClient;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @Mock
    private ExchangeMetrics exchangeMetrics;

    @InjectMocks
    private PlaceLimitOrderService placeLimitOrderService;

    @Test
    void reservesCashThroughFinanceThenPublishesBuyOrder() {
        UUID traderId = UUID.randomUUID();
        when(financeClient.reserveCash(new ReserveCashRequest(traderId, money("1000"))))
                .thenReturn(new ReserveCashResponse(money("1000")));

        var response = placeLimitOrderService.placeLimitOrder(new PlaceLimitOrderRequest(
                traderId,
                SYMBOL,
                OrderSide.BUY,
                money("100"),
                quantity("10")
        ));

        ArgumentCaptor<LimitOrderSubmittedEvent> eventCaptor =
                ArgumentCaptor.forClass(LimitOrderSubmittedEvent.class);
        verify(orderEventPublisher).publish(eventCaptor.capture());

        LimitOrderSubmittedEvent event = eventCaptor.getValue();
        assertThat(response.status()).isEqualTo(OrderStatus.ACCEPTED);
        assertThat(response.orderId()).isEqualTo(event.orderId());
        assertThat(event.reservedCash()).isEqualByComparingTo("1000");
        assertThat(event.reservedStock()).isNull();
        verify(exchangeMetrics).orderAccepted();
    }

    @Test
    void reservesStockThroughFinanceThenPublishesSellOrder() {
        UUID traderId = UUID.randomUUID();
        when(financeClient.reserveStock(new ReserveStockRequest(traderId, SYMBOL, quantity("4"))))
                .thenReturn(new ReserveStockResponse(quantity("4")));

        placeLimitOrderService.placeLimitOrder(new PlaceLimitOrderRequest(
                traderId,
                SYMBOL,
                OrderSide.SELL,
                money("100"),
                quantity("4")
        ));

        ArgumentCaptor<LimitOrderSubmittedEvent> eventCaptor =
                ArgumentCaptor.forClass(LimitOrderSubmittedEvent.class);
        verify(orderEventPublisher).publish(eventCaptor.capture());

        LimitOrderSubmittedEvent event = eventCaptor.getValue();
        assertThat(event.reservedCash()).isNull();
        assertThat(event.reservedStock()).isEqualByComparingTo("4");
        verify(exchangeMetrics).orderAccepted();
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private static BigDecimal quantity(String value) {
        return new BigDecimal(value);
    }
}

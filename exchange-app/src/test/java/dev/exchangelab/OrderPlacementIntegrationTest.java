package dev.exchangelab;

import dev.exchangelab.application.MatchAcceptedOrderUseCase;
import dev.exchangelab.application.OrderAcceptedEventPublisher;
import dev.exchangelab.application.PlaceLimitOrderUseCase;
import dev.exchangelab.domain.model.Order;
import dev.exchangelab.infrastructure.persistence.dao.OrderDao;
import dev.exchangelab.infrastructure.persistence.dao.StockPositionDao;
import dev.exchangelab.infrastructure.persistence.dao.TradeDao;
import dev.exchangelab.infrastructure.persistence.dao.TraderAccountDao;
import dev.exchangelab.infrastructure.persistence.entity.OrderEntity;
import dev.exchangelab.infrastructure.persistence.entity.StockPositionEntity;
import dev.exchangelab.infrastructure.persistence.entity.TradeEntity;
import dev.exchangelab.infrastructure.persistence.entity.TraderAccountEntity;
import dev.exchangelab.presentation.dto.PlaceLimitOrderRequest;
import dev.exchangelab.presentation.dto.PlaceLimitOrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class OrderPlacementIntegrationTest {

    private static final String SYMBOL = "ACME";

    @Container
    @ServiceConnection
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:18");

    @Autowired
    private PlaceLimitOrderUseCase placeLimitOrderUseCase;

    @Autowired
    private MatchAcceptedOrderUseCase matchAcceptedOrderUseCase;

    @Autowired
    private OrderDao orderDao;

    @Autowired
    private TraderAccountDao traderAccountDao;

    @Autowired
    private StockPositionDao stockPositionDao;

    @Autowired
    private TradeDao tradeDao;

    @BeforeEach
    void cleanDatabase() {
        tradeDao.deleteAll();
        orderDao.deleteAll();
        stockPositionDao.deleteAll();
        traderAccountDao.deleteAll();
    }

    @Test
    void acceptsUnmatchedBuyOrderAndReservesCash() {
        UUID buyerId = traderWithCash("10000");

        PlaceLimitOrderResponse response = placeLimitOrderUseCase.placeLimitOrder(
                new PlaceLimitOrderRequest(
                        buyerId,
                        SYMBOL,
                        Order.Side.BUY,
                        money("100"),
                        quantity("10")
                )
        );

        OrderEntity order = orderDao.findById(response.orderId()).orElseThrow();
        TraderAccountEntity buyerAccount = traderAccountDao.findById(buyerId).orElseThrow();

        assertThat(response.status()).isEqualTo(Order.Status.ACCEPTED);
        assertThat(order.getStatus()).isEqualTo(Order.Status.ACCEPTED);
        assertThat(order.getRemainingQuantity()).isEqualByComparingTo("10");
        assertThat(buyerAccount.getCashBalance()).isEqualByComparingTo("10000");
        assertThat(buyerAccount.getReservedCash()).isEqualByComparingTo("1000");
        assertThat(tradeDao.findAll()).isEmpty();
    }

    @Test
    void matchesBuyOrderAgainstExistingSellOrderAndSettlesBalances() {
        UUID buyerId = traderWithCash("10000");
        UUID sellerId = traderWithCash("0");
        UUID sellOrderId = restingSellOrder(sellerId, "100", "10", Instant.parse("2026-01-01T00:00:00Z"));

        PlaceLimitOrderResponse response = placeLimitOrderUseCase.placeLimitOrder(
                new PlaceLimitOrderRequest(
                        buyerId,
                        SYMBOL,
                        Order.Side.BUY,
                        money("100"),
                        quantity("10")
                )
        );
        matchAcceptedOrderUseCase.matchAcceptedOrder(response.orderId());

        OrderEntity buyOrder = orderDao.findById(response.orderId()).orElseThrow();
        OrderEntity sellOrder = orderDao.findById(sellOrderId).orElseThrow();
        TradeEntity trade = singleTrade();
        TraderAccountEntity buyerAccount = traderAccountDao.findById(buyerId).orElseThrow();
        TraderAccountEntity sellerAccount = traderAccountDao.findById(sellerId).orElseThrow();
        StockPositionEntity buyerPosition = stockPositionDao.findByTraderIdAndSymbol(buyerId, SYMBOL).orElseThrow();
        StockPositionEntity sellerPosition = stockPositionDao.findByTraderIdAndSymbol(sellerId, SYMBOL).orElseThrow();

        assertThat(buyOrder.getStatus()).isEqualTo(Order.Status.FILLED);
        assertThat(sellOrder.getStatus()).isEqualTo(Order.Status.FILLED);
        assertThat(trade.getBuyOrderId()).isEqualTo(buyOrder.getOrderId());
        assertThat(trade.getSellOrderId()).isEqualTo(sellOrderId);
        assertThat(trade.getPrice()).isEqualByComparingTo("100");
        assertThat(trade.getQuantity()).isEqualByComparingTo("10");
        assertThat(buyerAccount.getCashBalance()).isEqualByComparingTo("9000");
        assertThat(buyerAccount.getReservedCash()).isEqualByComparingTo("0");
        assertThat(sellerAccount.getCashBalance()).isEqualByComparingTo("1000");
        assertThat(buyerPosition.getQuantity()).isEqualByComparingTo("10");
        assertThat(sellerPosition.getQuantity()).isEqualByComparingTo("0");
        assertThat(sellerPosition.getReservedQuantity()).isEqualByComparingTo("0");
    }

    @Test
    void matchesCheapestSellOrderBeforeMoreExpensiveSellOrder() {
        UUID buyerId = traderWithCash("10000");
        UUID expensiveSellerId = traderWithCash("0");
        UUID cheapSellerId = traderWithCash("0");
        UUID expensiveSellOrderId = restingSellOrder(
                expensiveSellerId,
                "105",
                "10",
                Instant.parse("2026-01-01T00:00:00Z")
        );
        UUID cheapSellOrderId = restingSellOrder(
                cheapSellerId,
                "100",
                "10",
                Instant.parse("2026-01-01T00:01:00Z")
        );

        PlaceLimitOrderResponse response = placeLimitOrderUseCase.placeLimitOrder(
                new PlaceLimitOrderRequest(
                        buyerId,
                        SYMBOL,
                        Order.Side.BUY,
                        money("110"),
                        quantity("10")
                )
        );
        matchAcceptedOrderUseCase.matchAcceptedOrder(response.orderId());

        TradeEntity trade = singleTrade();
        OrderEntity cheapSellOrder = orderDao.findById(cheapSellOrderId).orElseThrow();
        OrderEntity expensiveSellOrder = orderDao.findById(expensiveSellOrderId).orElseThrow();

        assertThat(trade.getSellOrderId()).isEqualTo(cheapSellOrderId);
        assertThat(trade.getPrice()).isEqualByComparingTo("100");
        assertThat(cheapSellOrder.getStatus()).isEqualTo(Order.Status.FILLED);
        assertThat(expensiveSellOrder.getStatus()).isEqualTo(Order.Status.ACCEPTED);
    }

    @Test
    void matchesOldestSellOrderWhenPricesAreEqual() {
        UUID buyerId = traderWithCash("10000");
        UUID olderSellerId = traderWithCash("0");
        UUID newerSellerId = traderWithCash("0");
        UUID olderSellOrderId = restingSellOrder(
                olderSellerId,
                "100",
                "10",
                Instant.parse("2026-01-01T00:00:00Z")
        );
        UUID newerSellOrderId = restingSellOrder(
                newerSellerId,
                "100",
                "10",
                Instant.parse("2026-01-01T00:01:00Z")
        );

        PlaceLimitOrderResponse response = placeLimitOrderUseCase.placeLimitOrder(
                new PlaceLimitOrderRequest(
                        buyerId,
                        SYMBOL,
                        Order.Side.BUY,
                        money("100"),
                        quantity("10")
                )
        );
        matchAcceptedOrderUseCase.matchAcceptedOrder(response.orderId());

        TradeEntity trade = singleTrade();
        OrderEntity olderSellOrder = orderDao.findById(olderSellOrderId).orElseThrow();
        OrderEntity newerSellOrder = orderDao.findById(newerSellOrderId).orElseThrow();

        assertThat(trade.getSellOrderId()).isEqualTo(olderSellOrderId);
        assertThat(olderSellOrder.getStatus()).isEqualTo(Order.Status.FILLED);
        assertThat(newerSellOrder.getStatus()).isEqualTo(Order.Status.ACCEPTED);
    }

    @Test
    void keepsIncomingBuyOrderPartiallyFilledWithRemainingReservedCash() {
        UUID buyerId = traderWithCash("10000");
        UUID sellerId = traderWithCash("0");
        UUID sellOrderId = restingSellOrder(sellerId, "100", "5", Instant.parse("2026-01-01T00:00:00Z"));

        PlaceLimitOrderResponse response = placeLimitOrderUseCase.placeLimitOrder(
                new PlaceLimitOrderRequest(
                        buyerId,
                        SYMBOL,
                        Order.Side.BUY,
                        money("100"),
                        quantity("10")
                )
        );
        matchAcceptedOrderUseCase.matchAcceptedOrder(response.orderId());

        OrderEntity buyOrder = orderDao.findById(response.orderId()).orElseThrow();
        OrderEntity sellOrder = orderDao.findById(sellOrderId).orElseThrow();
        TraderAccountEntity buyerAccount = traderAccountDao.findById(buyerId).orElseThrow();
        TradeEntity trade = singleTrade();

        assertThat(buyOrder.getStatus()).isEqualTo(Order.Status.PARTIALLY_FILLED);
        assertThat(buyOrder.getRemainingQuantity()).isEqualByComparingTo("5");
        assertThat(sellOrder.getStatus()).isEqualTo(Order.Status.FILLED);
        assertThat(trade.getQuantity()).isEqualByComparingTo("5");
        assertThat(buyerAccount.getCashBalance()).isEqualByComparingTo("9500");
        assertThat(buyerAccount.getReservedCash()).isEqualByComparingTo("500");
    }

    @Test
    void matchesPartiallyFilledRestingOrderLater() {
        UUID sellerId = traderWithCash("0");
        seedPosition(sellerId, "10", "0");
        PlaceLimitOrderResponse sellResponse = placeLimitOrderUseCase.placeLimitOrder(
                new PlaceLimitOrderRequest(
                        sellerId,
                        SYMBOL,
                        Order.Side.SELL,
                        money("100"),
                        quantity("10")
                )
        );
        matchAcceptedOrderUseCase.matchAcceptedOrder(sellResponse.orderId());
        UUID firstBuyerId = traderWithCash("10000");
        UUID secondBuyerId = traderWithCash("10000");

        PlaceLimitOrderResponse firstBuyResponse = placeLimitOrderUseCase.placeLimitOrder(
                new PlaceLimitOrderRequest(
                        firstBuyerId,
                        SYMBOL,
                        Order.Side.BUY,
                        money("100"),
                        quantity("4")
                )
        );
        matchAcceptedOrderUseCase.matchAcceptedOrder(firstBuyResponse.orderId());
        PlaceLimitOrderResponse secondBuyResponse = placeLimitOrderUseCase.placeLimitOrder(
                new PlaceLimitOrderRequest(
                        secondBuyerId,
                        SYMBOL,
                        Order.Side.BUY,
                        money("100"),
                        quantity("6")
                )
        );
        matchAcceptedOrderUseCase.matchAcceptedOrder(secondBuyResponse.orderId());

        OrderEntity sellOrder = orderDao.findById(sellResponse.orderId()).orElseThrow();
        OrderEntity secondBuyOrder = orderDao.findById(secondBuyResponse.orderId()).orElseThrow();
        StockPositionEntity sellerPosition = stockPositionDao.findByTraderIdAndSymbol(sellerId, SYMBOL).orElseThrow();
        List<TradeEntity> trades = tradeDao.findAll();

        assertThat(sellOrder.getStatus()).isEqualTo(Order.Status.FILLED);
        assertThat(sellOrder.getRemainingQuantity()).isEqualByComparingTo("0");
        assertThat(secondBuyOrder.getStatus()).isEqualTo(Order.Status.FILLED);
        assertThat(sellerPosition.getQuantity()).isEqualByComparingTo("0");
        assertThat(sellerPosition.getReservedQuantity()).isEqualByComparingTo("0");
        assertThat(trades).hasSize(2);
        assertThat(trades)
                .extracting(TradeEntity::getQuantity)
                .usingComparatorForType(BigDecimal::compareTo, BigDecimal.class)
                .containsExactlyInAnyOrder(quantity("4"), quantity("6"));
    }

    private UUID traderWithCash(String cashBalance) {
        UUID traderId = UUID.randomUUID();
        traderAccountDao.save(new TraderAccountEntity(
                traderId,
                money(cashBalance),
                money("0")
        ));
        return traderId;
    }

    private UUID restingSellOrder(UUID sellerId, String price, String orderQuantity, Instant createdAt) {
        seedPosition(sellerId, orderQuantity, orderQuantity);
        UUID orderId = UUID.randomUUID();
        orderDao.save(new OrderEntity(
                orderId,
                sellerId,
                SYMBOL,
                Order.Side.SELL,
                money(price),
                quantity(orderQuantity),
                quantity(orderQuantity),
                Order.Status.ACCEPTED,
                createdAt
        ));
        return orderId;
    }

    private void seedPosition(UUID traderId, String positionQuantity, String reservedQuantity) {
        stockPositionDao.save(new StockPositionEntity(
                UUID.randomUUID(),
                traderId,
                SYMBOL,
                quantity(positionQuantity),
                quantity(reservedQuantity)
        ));
    }

    private TradeEntity singleTrade() {
        List<TradeEntity> trades = tradeDao.findAll();

        assertThat(trades).hasSize(1);
        return trades.getFirst();
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private static BigDecimal quantity(String value) {
        return new BigDecimal(value);
    }

    @TestConfiguration
    static class KafkaTestConfiguration {

        @Bean
        @Primary
        OrderAcceptedEventPublisher orderAcceptedEventPublisher() {
            return event -> {
            };
        }
    }
}

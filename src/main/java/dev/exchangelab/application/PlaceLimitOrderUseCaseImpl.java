package dev.exchangelab.application;

import dev.exchangelab.domain.model.MatchResult;
import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.OrderBook;
import dev.exchangelab.domain.model.Portfolio;
import dev.exchangelab.domain.model.Settlement;
import dev.exchangelab.domain.model.StockPosition;
import dev.exchangelab.domain.model.Trade;
import dev.exchangelab.domain.model.TraderAccount;
import dev.exchangelab.domain.repository.OrderRepository;
import dev.exchangelab.domain.repository.StockPositionRepository;
import dev.exchangelab.domain.repository.TradeRepository;
import dev.exchangelab.domain.repository.TraderAccountRepository;
import dev.exchangelab.presentation.PlaceLimitOrderRequest;
import dev.exchangelab.presentation.PlaceLimitOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaceLimitOrderUseCaseImpl implements PlaceLimitOrderUseCase {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final TraderAccountRepository traderAccountRepository;
    private final StockPositionRepository stockPositionRepository;

    @Override
    @Transactional
    public PlaceLimitOrderResponse placeLimitOrder(PlaceLimitOrderRequest request) {
        requireRequest(request);

        Order incomingOrder = Order.createLimit(
                request.traderId(),
                request.symbol(),
                request.side(),
                request.limitPrice(),
                request.quantity()
        );
        Portfolio portfolio = loadPortfolioFor(incomingOrder);
        portfolio.reserveFor(incomingOrder);
        savePortfolio(portfolio);

        OrderBook orderBook = orderRepository.findOrderBookFor(incomingOrder);
        MatchResult matchResult = orderBook.match(incomingOrder);

        Settlement settlement = loadSettlementFor(matchResult);
        settlement.settle(matchResult);
        saveSettlement(settlement);

        orderRepository.saveAll(matchResult.ordersToSave());
        tradeRepository.saveAll(matchResult.executedTrades());

        return PlaceLimitOrderResponse.from(matchResult.incomingOrder());
    }

    private void requireRequest(PlaceLimitOrderRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Order request is required");
        }
    }

    private Portfolio loadPortfolioFor(Order order) {
        return switch (order.getSide()) {
            case BUY -> Portfolio.cash(
                    traderAccountRepository.findForCashReservation(order.getTraderId())
                            .orElseThrow(() -> new IllegalStateException("Trader account not found"))
            );
            case SELL -> Portfolio.stock(
                    stockPositionRepository.findForStockReservation(order.getTraderId(), order.getSymbol())
                            .orElseThrow(() -> new IllegalStateException("Trader stock position not found"))
            );
        };
    }

    private void savePortfolio(Portfolio portfolio) {
        portfolio.traderAccountToSave().ifPresent(traderAccountRepository::save);
        portfolio.stockPositionToSave().ifPresent(stockPositionRepository::save);
    }

    private Settlement loadSettlementFor(MatchResult matchResult) {
        Map<UUID, TraderAccount> accountsByTraderId = new HashMap<>();
        Map<StockPosition.Key, StockPosition> positionsByKey = new HashMap<>();

        for (Trade trade : matchResult.executedTrades()) {
            accountsByTraderId.putIfAbsent(
                    trade.getBuyerTraderId(),
                    findTraderAccount(trade.getBuyerTraderId(), "Buyer account not found")
            );
            accountsByTraderId.putIfAbsent(
                    trade.getSellerTraderId(),
                    findTraderAccount(trade.getSellerTraderId(), "Seller account not found")
            );

            StockPosition sellerPosition = findSellerPosition(trade);
            positionsByKey.putIfAbsent(sellerPosition.key(), sellerPosition);

            StockPosition buyerPosition = findBuyerPosition(trade);
            positionsByKey.putIfAbsent(buyerPosition.key(), buyerPosition);
        }

        return new Settlement(
                new ArrayList<>(accountsByTraderId.values()),
                new ArrayList<>(positionsByKey.values())
        );
    }

    private void saveSettlement(Settlement settlement) {
        settlement.accountsToSave().forEach(traderAccountRepository::save);
        settlement.positionsToSave().forEach(stockPositionRepository::save);
    }

    private TraderAccount findTraderAccount(UUID traderId, String message) {
        return traderAccountRepository.findForCashReservation(traderId)
                .orElseThrow(() -> new IllegalStateException(message));
    }

    private StockPosition findSellerPosition(Trade trade) {
        return stockPositionRepository.findForStockReservation(
                        trade.getSellerTraderId(),
                        trade.getSymbol()
                )
                .orElseThrow(() -> new IllegalStateException("Seller stock position not found"));
    }

    private StockPosition findBuyerPosition(Trade trade) {
        return stockPositionRepository.findForStockReservation(
                        trade.getBuyerTraderId(),
                        trade.getSymbol()
                )
                .orElseGet(() -> new StockPosition(
                        UUID.randomUUID(),
                        trade.getBuyerTraderId(),
                        trade.getSymbol(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ));
    }
}

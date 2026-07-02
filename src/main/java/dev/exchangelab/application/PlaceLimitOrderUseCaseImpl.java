package dev.exchangelab.application;

import dev.exchangelab.domain.model.MatchResult;
import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.OrderBook;
import dev.exchangelab.domain.model.Settlement;
import dev.exchangelab.domain.model.StockPosition;
import dev.exchangelab.domain.model.TraderAccount;
import dev.exchangelab.domain.repository.OrderRepository;
import dev.exchangelab.domain.repository.StockPositionRepository;
import dev.exchangelab.domain.repository.TradeRepository;
import dev.exchangelab.domain.repository.TraderAccountRepository;
import dev.exchangelab.presentation.dto.PlaceLimitOrderRequest;
import dev.exchangelab.presentation.dto.PlaceLimitOrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PlaceLimitOrderUseCaseImpl implements PlaceLimitOrderUseCase {

    private final OrderRepository orderRepository;
    private final TradeRepository tradeRepository;
    private final TraderAccountRepository traderAccountRepository;
    private final StockPositionRepository stockPositionRepository;
    private final SettlementStore settlementStore;

    @Override
    @Transactional
    public PlaceLimitOrderResponse placeLimitOrder(PlaceLimitOrderRequest request) {
        // Stage 1: Receive order
        Order incomingOrder = Order.createLimit(
                request.traderId(),
                request.symbol(),
                request.side(),
                request.limitPrice(),
                request.quantity()
        );

        // Stage 2: Reserve asset
        switch (incomingOrder.getSide()) {
            case BUY -> {
                TraderAccount traderAccount = traderAccountRepository
                        .findForCashReservation(incomingOrder.getTraderId())
                        .orElseThrow(() -> new IllegalStateException("Trader account not found"));

                traderAccount.reserveCash(
                        incomingOrder.getLimitPrice().multiply(incomingOrder.getQuantity())
                );
                traderAccountRepository.save(traderAccount);
            }
            case SELL -> {
                StockPosition stockPosition = stockPositionRepository
                        .findForStockReservation(
                                incomingOrder.getTraderId(),
                                incomingOrder.getSymbol()
                        )
                        .orElseThrow(() -> new IllegalStateException("Trader stock position not found"));

                stockPosition.reserve(incomingOrder.getQuantity());
                stockPositionRepository.save(stockPosition);
            }
        }

        // Stage 3: Match order
        OrderBook orderBook = orderRepository.findOrderBookFor(incomingOrder);
        MatchResult matchResult = orderBook.match(incomingOrder);

        // Stage 4: Apply result
        Settlement settlement = settlementStore.loadFor(matchResult);
        settlement.settle(matchResult);
        settlementStore.save(settlement);

        orderRepository.saveAll(matchResult.ordersToSave());
        tradeRepository.saveAll(matchResult.executedTrades());

        // Stage 5: Return response
        return PlaceLimitOrderResponse.from(matchResult.incomingOrder());
    }
}

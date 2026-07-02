package dev.exchangelab.application;

import dev.exchangelab.domain.model.MatchResult;
import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.OrderBook;
import dev.exchangelab.domain.model.Portfolio;
import dev.exchangelab.domain.model.Settlement;
import dev.exchangelab.domain.repository.OrderRepository;
import dev.exchangelab.domain.repository.TradeRepository;
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
    private final PortfolioStore portfolioStore;
    private final SettlementStore settlementStore;

    @Override
    @Transactional
    public PlaceLimitOrderResponse placeLimitOrder(PlaceLimitOrderRequest request) {
        Order incomingOrder = Order.createLimit(
                request.traderId(),
                request.symbol(),
                request.side(),
                request.limitPrice(),
                request.quantity()
        );
        Portfolio portfolio = portfolioStore.loadFor(incomingOrder);
        portfolio.reserveFor(incomingOrder);
        portfolioStore.save(portfolio);

        OrderBook orderBook = orderRepository.findOrderBookFor(incomingOrder);
        MatchResult matchResult = orderBook.match(incomingOrder);

        Settlement settlement = settlementStore.loadFor(matchResult);
        settlement.settle(matchResult);
        settlementStore.save(settlement);

        orderRepository.saveAll(matchResult.ordersToSave());
        tradeRepository.saveAll(matchResult.executedTrades());

        return PlaceLimitOrderResponse.from(matchResult.incomingOrder());
    }
}

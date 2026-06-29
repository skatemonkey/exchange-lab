package dev.exchangelab.application;

import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.Portfolio;
import dev.exchangelab.domain.repository.StockPositionRepository;
import dev.exchangelab.domain.repository.TraderAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PortfolioStore {

    private final TraderAccountRepository traderAccountRepository;
    private final StockPositionRepository stockPositionRepository;

    public Portfolio loadFor(Order order) {
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

    public void save(Portfolio portfolio) {
        portfolio.traderAccountToSave().ifPresent(traderAccountRepository::save);
        portfolio.stockPositionToSave().ifPresent(stockPositionRepository::save);
    }
}

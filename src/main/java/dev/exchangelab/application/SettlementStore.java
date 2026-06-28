package dev.exchangelab.application;

import dev.exchangelab.domain.model.MatchResult;
import dev.exchangelab.domain.model.Settlement;
import dev.exchangelab.domain.model.StockPosition;
import dev.exchangelab.domain.model.Trade;
import dev.exchangelab.domain.model.TraderAccount;
import dev.exchangelab.domain.repository.StockPositionRepository;
import dev.exchangelab.domain.repository.TraderAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SettlementStore {

    private final TraderAccountRepository traderAccountRepository;
    private final StockPositionRepository stockPositionRepository;

    public Settlement loadFor(MatchResult matchResult) {
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

    public void save(Settlement settlement) {
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

package dev.exchangelab.domain.service;

import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.StockPosition;
import dev.exchangelab.domain.model.TraderAccount;
import dev.exchangelab.domain.repository.StockPositionRepository;
import dev.exchangelab.domain.repository.TraderAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderReservationService {

    private final TraderAccountRepository traderAccountRepository;
    private final StockPositionRepository stockPositionRepository;

    public void reserve(Order order) {
        switch (order.getSide()) {
            case BUY -> reserveCash(order.getTraderId(), order.getLimitPrice(), order.getQuantity());
            case SELL -> reserveStock(order.getTraderId(), order.getSymbol(), order.getQuantity());
        }
    }

    private void reserveCash(UUID traderId, BigDecimal limitPrice, BigDecimal quantity) {
        BigDecimal requiredCash = limitPrice.multiply(quantity);
        TraderAccount account = traderAccountRepository.findForCashReservation(traderId)
                .orElseThrow(() -> new IllegalStateException("Trader account not found"));

        account.reserveCash(requiredCash);
        traderAccountRepository.save(account);
    }

    private void reserveStock(UUID traderId, String symbol, BigDecimal quantity) {
        StockPosition position = stockPositionRepository.findForStockReservation(traderId, symbol)
                .orElseThrow(() -> new IllegalStateException("Trader stock position not found"));

        position.reserve(quantity);
        stockPositionRepository.save(position);
    }
}

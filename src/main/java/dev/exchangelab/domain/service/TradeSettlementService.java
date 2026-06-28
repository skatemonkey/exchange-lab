package dev.exchangelab.domain.service;

import dev.exchangelab.domain.model.Order;
import dev.exchangelab.domain.model.StockPosition;
import dev.exchangelab.domain.model.Trade;
import dev.exchangelab.domain.model.TraderAccount;
import dev.exchangelab.domain.repository.StockPositionRepository;
import dev.exchangelab.domain.repository.TraderAccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TradeSettlementService {

    private final TraderAccountRepository traderAccountRepository;
    private final StockPositionRepository stockPositionRepository;

    public void settle(
            List<Trade> trades,
            Order incomingOrder,
            List<Order> matchingOrders
    ) {
        Map<UUID, Order> ordersById = new HashMap<>();
        ordersById.put(incomingOrder.getOrderId(), incomingOrder);
        matchingOrders.forEach(order -> ordersById.put(order.getOrderId(), order));

        for (Trade trade : trades) {
            Order buyOrder = ordersById.get(trade.getBuyOrderId());

            settleCash(trade, buyOrder);
            settleStock(trade);
        }
    }

    private void settleCash(Trade trade, Order buyOrder) {
        BigDecimal tradeValue = trade.getPrice().multiply(trade.getQuantity());
        BigDecimal reservedCashToRelease = buyOrder.getLimitPrice().multiply(trade.getQuantity());

        TraderAccount buyerAccount = traderAccountRepository.findForCashReservation(trade.getBuyerTraderId())
                .orElseThrow(() -> new IllegalStateException("Buyer account not found"));
        TraderAccount sellerAccount = traderAccountRepository.findForCashReservation(trade.getSellerTraderId())
                .orElseThrow(() -> new IllegalStateException("Seller account not found"));

        buyerAccount.settleBuy(tradeValue, reservedCashToRelease);
        sellerAccount.receiveCash(tradeValue);

        traderAccountRepository.save(buyerAccount);
        traderAccountRepository.save(sellerAccount);
    }

    private void settleStock(Trade trade) {
        StockPosition sellerPosition = stockPositionRepository.findForStockReservation(
                        trade.getSellerTraderId(),
                        trade.getSymbol()
                )
                .orElseThrow(() -> new IllegalStateException("Seller stock position not found"));
        StockPosition buyerPosition = stockPositionRepository.findForStockReservation(
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

        sellerPosition.settleSell(trade.getQuantity());
        buyerPosition.receive(trade.getQuantity());

        stockPositionRepository.save(sellerPosition);
        stockPositionRepository.save(buyerPosition);
    }
}

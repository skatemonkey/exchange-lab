package dev.exchangelab.finance.application;

import dev.exchangelab.common.event.TradeMatchedEvent;
import dev.exchangelab.finance.persistence.StockPositionEntity;
import dev.exchangelab.finance.persistence.StockPositionRepository;
import dev.exchangelab.finance.persistence.TraderAccountEntity;
import dev.exchangelab.finance.persistence.TraderAccountRepository;
import dev.exchangelab.finance.redis.RedisReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinanceSettlementService {

    private final TraderAccountRepository traderAccountRepository;
    private final StockPositionRepository stockPositionRepository;
    private final RedisReservationService redisReservationService;

    @Transactional
    public void settle(TradeMatchedEvent event) {
        TraderAccountEntity buyerAccount = traderAccountRepository.findByIdForUpdate(event.buyerTraderId())
                .orElseThrow(() -> new IllegalStateException("Buyer account not found"));
        TraderAccountEntity sellerAccount = traderAccountRepository.findByIdForUpdate(event.sellerTraderId())
                .orElseThrow(() -> new IllegalStateException("Seller account not found"));

        StockPositionEntity sellerPosition = stockPositionRepository
                .findByTraderIdAndSymbolForUpdate(event.sellerTraderId(), event.symbol())
                .orElseThrow(() -> new IllegalStateException("Seller stock position not found"));
        StockPositionEntity buyerPosition = stockPositionRepository
                .findByTraderIdAndSymbolForUpdate(event.buyerTraderId(), event.symbol())
                .orElseGet(() -> new StockPositionEntity(
                        UUID.randomUUID(),
                        event.buyerTraderId(),
                        event.symbol(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO
                ));

        BigDecimal buyerCashAvailableBefore = buyerAccount.availableCash();
        BigDecimal sellerCashAvailableBefore = sellerAccount.availableCash();
        BigDecimal buyerStockAvailableBefore = buyerPosition.availableQuantity();
        BigDecimal tradeValue = event.price().multiply(event.quantity());
        BigDecimal reservedCashToRelease = event.buyOrderLimitPrice().multiply(event.quantity());

        buyerAccount.settleBuy(tradeValue, reservedCashToRelease);
        sellerAccount.receiveCash(tradeValue);
        sellerPosition.settleSell(event.quantity());
        buyerPosition.receive(event.quantity());

        traderAccountRepository.saveAll(List.of(buyerAccount, sellerAccount));
        stockPositionRepository.saveAll(List.of(sellerPosition, buyerPosition));

        redisReservationService.increaseAvailableCash(
                event.sellerTraderId(),
                tradeValue,
                sellerCashAvailableBefore
        );
        redisReservationService.increaseAvailableStock(
                event.buyerTraderId(),
                event.symbol(),
                event.quantity(),
                buyerStockAvailableBefore
        );

        BigDecimal unusedReservedCash = reservedCashToRelease.subtract(tradeValue);
        if (unusedReservedCash.compareTo(BigDecimal.ZERO) > 0) {
            redisReservationService.increaseAvailableCash(
                    event.buyerTraderId(),
                    unusedReservedCash,
                    buyerCashAvailableBefore
            );
        }
    }
}

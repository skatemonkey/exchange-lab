package dev.exchangelab.infrastructure.persistence.repository;

import dev.exchangelab.domain.model.Trade;
import dev.exchangelab.domain.repository.TradeRepository;
import dev.exchangelab.infrastructure.persistence.dao.TradeDao;
import dev.exchangelab.infrastructure.persistence.entity.TradeEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JpaTradeRepository implements TradeRepository {

    private final TradeDao tradeDao;

    @Override
    public void saveAll(List<Trade> trades) {
        tradeDao.saveAll(trades.stream().map(this::toEntity).toList());
    }

    private TradeEntity toEntity(Trade trade) {
        return new TradeEntity(
                trade.getTradeId(),
                trade.getBuyOrderId(),
                trade.getSellOrderId(),
                trade.getBuyerTraderId(),
                trade.getSellerTraderId(),
                trade.getSymbol(),
                trade.getPrice(),
                trade.getQuantity(),
                trade.getCreatedAt()
        );
    }
}

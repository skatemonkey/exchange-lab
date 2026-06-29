package dev.exchangelab.infrastructure.persistence.query;

import dev.exchangelab.infrastructure.persistence.entity.TradeEntity;

import java.util.List;

public interface TradeQuery {

    List<TradeEntity> findLatestTradesBySymbol(String symbol, int limit);
}

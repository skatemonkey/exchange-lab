package dev.exchangelab.repository.query;

import dev.exchangelab.model.entity.TradeEntity;

import java.util.List;

public interface TradeQuery {

    List<TradeEntity> findLatestTradesBySymbol(String symbol, int limit);
}

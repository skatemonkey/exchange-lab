package dev.exchangelab.match.domain.repository;

import dev.exchangelab.match.domain.model.Trade;

import java.util.List;

public interface TradeRepository {

    void saveAll(List<Trade> trades);
}

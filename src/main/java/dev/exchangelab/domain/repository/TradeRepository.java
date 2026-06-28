package dev.exchangelab.domain.repository;

import dev.exchangelab.domain.model.Trade;

import java.util.List;

public interface TradeRepository {

    void saveAll(List<Trade> trades);
}

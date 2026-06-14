package dev.exchangelab.market;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class MatchingEngine {

    // ---------------------------------------------------------------------
    // Operations
    // ---------------------------------------------------------------------

    public List<Trade> process(Order incomingOrder, OrderBook orderBook) {
        Objects.requireNonNull(incomingOrder);
        Objects.requireNonNull(orderBook);

        orderBook.add(incomingOrder);
        return Collections.emptyList();
    }
}

package dev.exchangelab.market.application;

import dev.exchangelab.market.Order;
import dev.exchangelab.market.application.dto.PlaceLimitOrderCommand;
import dev.exchangelab.market.application.dto.PlaceLimitOrderResult;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class PlaceLimitOrderUseCase {

    // ---------------------------------------------------------------------
    // Operations
    // ---------------------------------------------------------------------

    public PlaceLimitOrderResult place(PlaceLimitOrderCommand command) {
        Objects.requireNonNull(command);

        Order order = Order.place(
                command.traderId(),
                command.stockSymbol(),
                command.side(),
                command.limitPrice(),
                command.quantity()
        );

        return PlaceLimitOrderResult.from(order);
    }
}

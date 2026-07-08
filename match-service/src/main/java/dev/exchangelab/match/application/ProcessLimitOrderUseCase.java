package dev.exchangelab.match.application;

import dev.exchangelab.common.event.LimitOrderSubmittedEvent;
import dev.exchangelab.match.domain.model.Order;

public interface ProcessLimitOrderUseCase {

    Order process(LimitOrderSubmittedEvent event);
}

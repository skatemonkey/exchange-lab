package dev.exchangelab.application;

import dev.exchangelab.common.event.LimitOrderSubmittedEvent;
import dev.exchangelab.domain.model.Order;

public interface ProcessLimitOrderUseCase {

    Order process(LimitOrderSubmittedEvent event);
}

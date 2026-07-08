package dev.exchangelab.application;

import dev.exchangelab.common.event.LimitOrderSubmittedEvent;

public interface OrderEventPublisher {

    void publish(LimitOrderSubmittedEvent event);
}

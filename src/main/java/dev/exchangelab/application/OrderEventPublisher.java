package dev.exchangelab.application;

import dev.exchangelab.application.event.LimitOrderSubmittedEvent;

public interface OrderEventPublisher {

    void publish(LimitOrderSubmittedEvent event);
}

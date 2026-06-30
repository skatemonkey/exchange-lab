package dev.exchangelab.application;

import dev.exchangelab.domain.event.OrderAcceptedEvent;

public interface OrderAcceptedEventPublisher {

    void publish(OrderAcceptedEvent event);
}

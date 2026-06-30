package dev.exchangelab.infrastructure.messaging.kafka;

import dev.exchangelab.application.OrderAcceptedEventPublisher;
import dev.exchangelab.domain.event.OrderAcceptedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Component
@RequiredArgsConstructor
public class KafkaOrderAcceptedEventPublisher implements OrderAcceptedEventPublisher {

    private final KafkaTemplate<String, OrderAcceptedEvent> kafkaTemplate;

    @Override
    public void publish(OrderAcceptedEvent event) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(event);
                }
            });
            return;
        }

        send(event);
    }

    private void send(OrderAcceptedEvent event) {
        kafkaTemplate.send(KafkaTopicConfig.ORDERS_ACCEPTED_TOPIC, event.symbol(), event);
    }
}

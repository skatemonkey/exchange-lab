package dev.exchangelab.exchange.kafka;

import dev.exchangelab.common.event.LimitOrderSubmittedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
public class OrderEventPublisher {

    private final KafkaTemplate<String, LimitOrderSubmittedEvent> kafkaTemplate;

    public void publish(LimitOrderSubmittedEvent event) {
        try {
            kafkaTemplate.send(
                    KafkaTopicConfig.ORDERS_SUBMITTED_TOPIC,
                    event.orderId().toString(),
                    event
            ).get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing order event", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Failed to publish order event", exception);
        }
    }
}

package dev.exchangelab.match.kafka;

import dev.exchangelab.common.event.TradeMatchedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
public class TradeEventPublisher {

    private final KafkaTemplate<String, TradeMatchedEvent> kafkaTemplate;

    public void publish(TradeMatchedEvent event) {
        try {
            kafkaTemplate.send(
                    KafkaTopicConfig.TRADES_MATCHED_TOPIC,
                    event.tradeId().toString(),
                    event
            ).get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while publishing trade event", exception);
        } catch (ExecutionException exception) {
            throw new IllegalStateException("Failed to publish trade event", exception);
        }
    }
}

package dev.exchangelab.infrastructure.messaging.kafka;

import dev.exchangelab.application.MatchAcceptedOrderUseCase;
import dev.exchangelab.domain.event.OrderAcceptedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderAcceptedKafkaConsumer {

    private final MatchAcceptedOrderUseCase matchAcceptedOrderUseCase;

    @KafkaListener(
            topics = KafkaTopicConfig.ORDERS_ACCEPTED_TOPIC,
            groupId = "${exchange.kafka.consumer.matching-group:exchange-lab-matching}"
    )
    public void consume(OrderAcceptedEvent event) {
        matchAcceptedOrderUseCase.matchAcceptedOrder(event.orderId());
    }
}

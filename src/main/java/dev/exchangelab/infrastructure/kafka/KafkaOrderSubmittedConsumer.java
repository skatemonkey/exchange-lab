package dev.exchangelab.infrastructure.kafka;

import dev.exchangelab.application.ProcessLimitOrderUseCase;
import dev.exchangelab.common.event.LimitOrderSubmittedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaOrderSubmittedConsumer {

    private final ProcessLimitOrderUseCase processLimitOrderUseCase;

    @KafkaListener(
            topics = KafkaTopicConfig.ORDERS_SUBMITTED_TOPIC,
            groupId = "${spring.kafka.consumer.group-id:exchange-lab-order-matcher}"
    )
    public void consume(LimitOrderSubmittedEvent event) {
        processLimitOrderUseCase.process(event);
    }
}

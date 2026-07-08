package dev.exchangelab.finance.kafka;

import dev.exchangelab.common.event.TradeMatchedEvent;
import dev.exchangelab.finance.application.FinanceSettlementService;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaTradeMatchedConsumer {

    private final FinanceSettlementService financeSettlementService;

    @KafkaListener(
            topics = KafkaTopicConfig.TRADES_MATCHED_TOPIC,
            groupId = "${spring.kafka.consumer.group-id:exchange-lab-trade-settler}"
    )
    public void consume(TradeMatchedEvent event) {
        financeSettlementService.settle(event);
    }
}

package dev.exchangelab.infrastructure.messaging.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String ORDERS_ACCEPTED_TOPIC = "orders.accepted";

    @Bean
    NewTopic ordersAcceptedTopic() {
        return TopicBuilder.name(ORDERS_ACCEPTED_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}

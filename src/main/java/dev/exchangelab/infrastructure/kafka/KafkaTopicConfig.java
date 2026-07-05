package dev.exchangelab.infrastructure.kafka;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaAdmin;
import org.springframework.kafka.config.TopicBuilder;

import java.util.Map;

@Configuration
public class KafkaTopicConfig {

    public static final String ORDERS_SUBMITTED_TOPIC = "orders.submitted";

    @Bean
    KafkaAdmin kafkaAdmin(
            @Value("${spring.kafka.bootstrap-servers:localhost:9092}") String bootstrapServers,
            @Value("${spring.kafka.admin.fail-fast:true}") boolean failFast
    ) {
        KafkaAdmin kafkaAdmin = new KafkaAdmin(Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        ));
        kafkaAdmin.setFatalIfBrokerNotAvailable(failFast);
        return kafkaAdmin;
    }

    @Bean
    NewTopic ordersSubmittedTopic() {
        return TopicBuilder.name(ORDERS_SUBMITTED_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}

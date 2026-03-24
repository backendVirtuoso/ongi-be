package com.ongi.infra.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String EMAIL_SEND_TOPIC = "ongi.email.send";
    public static final String EMAIL_SEND_DLQ_TOPIC = "ongi.email.send.dlq";

    @Bean
    public NewTopic emailSendTopic() {
        return TopicBuilder.name(EMAIL_SEND_TOPIC)
                .partitions(6)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic emailSendDlqTopic() {
        return TopicBuilder.name(EMAIL_SEND_DLQ_TOPIC)
                .partitions(1)
                .replicas(1)
                .build();
    }
}

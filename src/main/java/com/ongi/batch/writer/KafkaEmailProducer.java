package com.ongi.batch.writer;

import com.ongi.batch.dto.EmailMessageDto;
import com.ongi.infra.kafka.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEmailProducer implements ItemWriter<EmailMessageDto> {

    private final KafkaTemplate<String, EmailMessageDto> kafkaTemplate;

    @Override
    public void write(Chunk<? extends EmailMessageDto> chunk) {
        for (EmailMessageDto dto : chunk) {
            kafkaTemplate.send(KafkaTopicConfig.EMAIL_SEND_TOPIC, String.valueOf(dto.subscriberId()), dto);
        }
        log.debug("Published {} email messages to Kafka", chunk.size());
    }
}

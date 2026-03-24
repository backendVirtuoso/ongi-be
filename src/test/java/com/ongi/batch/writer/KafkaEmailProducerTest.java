package com.ongi.batch.writer;

import com.ongi.batch.dto.EmailMessageDto;
import com.ongi.infra.kafka.KafkaTopicConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaEmailProducerTest {

    @Mock
    private KafkaTemplate<String, EmailMessageDto> kafkaTemplate;

    @InjectMocks
    private KafkaEmailProducer kafkaEmailProducer;

    @Test
    @DisplayName("write()는 각 EmailMessageDto를 subscriberId 키로 Kafka에 발행한다")
    void write_SendsEachDtoToKafka() {
        EmailMessageDto dto1 = new EmailMessageDto(
                1L, "user1@example.com", "홍길동", 10L,
                "좋은 하루 되세요", "힐링", "morning",
                "[토닥토닥] 아침 문장", "email/morning-email.html"
        );
        EmailMessageDto dto2 = new EmailMessageDto(
                2L, "user2@example.com", "김철수", 11L,
                "힘내세요", "동기부여", "morning",
                "[토닥토닥] 아침 문장", "email/morning-email.html"
        );
        Chunk<EmailMessageDto> chunk = new Chunk<>(List.of(dto1, dto2));

        kafkaEmailProducer.write(chunk);

        verify(kafkaTemplate).send(KafkaTopicConfig.EMAIL_SEND_TOPIC, "1", dto1);
        verify(kafkaTemplate).send(KafkaTopicConfig.EMAIL_SEND_TOPIC, "2", dto2);
        verifyNoMoreInteractions(kafkaTemplate);
    }

    @Test
    @DisplayName("빈 Chunk로 write() 호출 시 Kafka 발행이 일어나지 않는다")
    void write_EmptyChunk_NoKafkaSend() {
        Chunk<EmailMessageDto> emptyChunk = new Chunk<>(List.of());

        kafkaEmailProducer.write(emptyChunk);

        verifyNoInteractions(kafkaTemplate);
    }
}

package com.ongi.infra.kafka;

import com.ongi.batch.dto.EmailMessageDto;
import com.ongi.domain.quote.entity.Category;
import com.ongi.domain.quote.entity.Quote;
import com.ongi.domain.quote.entity.SourceType;
import com.ongi.domain.quote.repository.QuoteRepository;
import com.ongi.domain.subscriber.entity.SendHistory;
import com.ongi.domain.subscriber.entity.Subscriber;
import com.ongi.domain.subscriber.repository.SendHistoryRepository;
import com.ongi.domain.subscriber.repository.SubscriberRepository;
import com.ongi.infra.mail.MailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.IContext;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KafkaEmailConsumerTest {

    @Mock
    private MailService mailService;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private SubscriberRepository subscriberRepository;

    @Mock
    private QuoteRepository quoteRepository;

    @Mock
    private SendHistoryRepository sendHistoryRepository;

    @Mock
    private Acknowledgment ack;

    @InjectMocks
    private KafkaEmailConsumer kafkaEmailConsumer;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(kafkaEmailConsumer, "frontendBaseUrl", "http://localhost:3000");
    }

    private EmailMessageDto buildDto() {
        return new EmailMessageDto(
                1L, "test@example.com", "홍길동", 10L,
                "좋은 하루 되세요", "힐링", "morning",
                "[토닥토닥] 아침 문장", "email/morning-email.html"
        );
    }

    @Test
    @DisplayName("consume() 성공 시 메일 발송, SendHistory 저장, ack.acknowledge()가 호출된다")
    void consume_Success_SendsMailAndRecordsHistory() {
        EmailMessageDto dto = buildDto();
        Subscriber subscriber = Subscriber.create("test@example.com", "홍길동", "token");
        Quote quote = Quote.create("좋은 하루 되세요", Category.COMFORT, SourceType.MANUAL);

        given(templateEngine.process(anyString(), any(IContext.class))).willReturn("<html>content</html>");
        given(subscriberRepository.findById(1L)).willReturn(Optional.of(subscriber));
        given(quoteRepository.findById(10L)).willReturn(Optional.of(quote));

        kafkaEmailConsumer.consume(dto, ack);

        verify(templateEngine).process(eq("email/morning-email.html"), any(IContext.class));
        verify(mailService).sendEmailWithContent("test@example.com", "[토닥토닥] 아침 문장", "<html>content</html>");

        ArgumentCaptor<SendHistory> historyCaptor = ArgumentCaptor.forClass(SendHistory.class);
        verify(sendHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getSendStatus()).isEqualTo("SUCCESS");
        assertThat(historyCaptor.getValue().getSendType()).isEqualTo("morning");

        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("consume() 실패 시 SendHistory에 FAILED가 기록되고 ack.acknowledge()는 여전히 호출된다")
    void consume_MailSendFails_RecordsFailureAndAcknowledges() {
        EmailMessageDto dto = buildDto();
        Subscriber subscriber = Subscriber.create("test@example.com", "홍길동", "token");
        Quote quote = Quote.create("좋은 하루 되세요", Category.COMFORT, SourceType.MANUAL);

        given(templateEngine.process(anyString(), any(IContext.class))).willReturn("<html>content</html>");
        doThrow(new RuntimeException("SMTP 연결 실패"))
                .when(mailService).sendEmailWithContent(anyString(), anyString(), anyString());
        given(subscriberRepository.findById(1L)).willReturn(Optional.of(subscriber));
        given(quoteRepository.findById(10L)).willReturn(Optional.of(quote));

        kafkaEmailConsumer.consume(dto, ack);

        ArgumentCaptor<SendHistory> historyCaptor = ArgumentCaptor.forClass(SendHistory.class);
        verify(sendHistoryRepository).save(historyCaptor.capture());
        assertThat(historyCaptor.getValue().getSendStatus()).isEqualTo("FAILED");
        assertThat(historyCaptor.getValue().getErrorMessage()).contains("SMTP 연결 실패");

        verify(ack).acknowledge();
    }

    @Test
    @DisplayName("consume() 실패 시 subscriber가 없으면 SendHistory 저장 없이 ack만 호출된다")
    void consume_MailFailsNoSubscriber_OnlyAcknowledges() {
        EmailMessageDto dto = buildDto();

        given(templateEngine.process(anyString(), any(IContext.class))).willReturn("<html>content</html>");
        doThrow(new RuntimeException("오류")).when(mailService).sendEmailWithContent(anyString(), anyString(), anyString());
        given(subscriberRepository.findById(1L)).willReturn(Optional.empty());

        kafkaEmailConsumer.consume(dto, ack);

        verify(sendHistoryRepository, never()).save(any());
        verify(ack).acknowledge();
    }
}

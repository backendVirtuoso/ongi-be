package com.ongi.infra.kafka;

import com.ongi.batch.dto.EmailMessageDto;
import com.ongi.domain.quote.entity.Quote;
import com.ongi.domain.quote.repository.QuoteRepository;
import com.ongi.domain.subscriber.entity.SendHistory;
import com.ongi.domain.subscriber.entity.Subscriber;
import com.ongi.domain.subscriber.repository.SendHistoryRepository;
import com.ongi.domain.subscriber.repository.SubscriberRepository;
import com.ongi.infra.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEmailConsumer {

    private final MailService mailService;
    private final TemplateEngine templateEngine;
    private final SubscriberRepository subscriberRepository;
    private final QuoteRepository quoteRepository;
    private final SendHistoryRepository sendHistoryRepository;

    @Value("${ongi.frontend.base-url}")
    private String frontendBaseUrl;

    @KafkaListener(
            topics = KafkaTopicConfig.EMAIL_SEND_TOPIC,
            groupId = "ongi-email-consumer",
            concurrency = "3",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(EmailMessageDto dto, Acknowledgment ack) {
        try {
            // Consumer에서 Thymeleaf 재렌더링 (메시지 크기 최적화)
            Context context = new Context();
            context.setVariable("name", dto.name() != null ? dto.name() : "구독자");
            context.setVariable("quoteContent", dto.quoteContent());
            context.setVariable("category", dto.category());
            context.setVariable("frontendBaseUrl", frontendBaseUrl);
            String html = templateEngine.process(dto.templateName(), context);

            mailService.sendEmailWithContent(dto.email(), dto.subject(), html);

            // SendHistory 기록
            subscriberRepository.findById(dto.subscriberId()).ifPresent(subscriber ->
                    quoteRepository.findById(dto.quoteId()).ifPresent(quote ->
                            sendHistoryRepository.save(SendHistory.success(subscriber, quote, dto.sendType()))
                    )
            );

            log.info("Email sent via Kafka: {} → {}", dto.sendType(), dto.email());
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", dto.email(), e.getMessage());
            recordFailure(dto, e.getMessage());
            throw e;
        }
    }

    private void recordFailure(EmailMessageDto dto, String errorMessage) {
        try {
            Subscriber subscriber = subscriberRepository.findById(dto.subscriberId()).orElse(null);
            Quote quote = quoteRepository.findById(dto.quoteId()).orElse(null);
            if (subscriber != null && quote != null) {
                sendHistoryRepository.save(SendHistory.failed(subscriber, quote, dto.sendType(), errorMessage));
            }
        } catch (Exception ex) {
            log.error("Failed to record send failure history: {}", ex.getMessage());
        }
    }
}

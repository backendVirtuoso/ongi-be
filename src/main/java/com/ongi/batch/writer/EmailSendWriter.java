package com.ongi.batch.writer;

import com.ongi.batch.processor.EmailMessage;
import com.ongi.domain.subscriber.entity.SendHistory;
import com.ongi.domain.subscriber.repository.SendHistoryRepository;
import com.ongi.infra.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.Chunk;
import org.springframework.batch.infrastructure.item.ItemWriter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailSendWriter implements ItemWriter<EmailMessage> {

    private final MailService mailService;
    private final SendHistoryRepository sendHistoryRepository;

    @Override
    public void write(@NonNull Chunk<? extends EmailMessage> chunk) {
        for (EmailMessage message : chunk) {
            try {
                mailService.sendEmailWithContent(
                        message.subscriber().getEmail(),
                        message.subject(),
                        message.html()
                );

                SendHistory history = SendHistory.success(
                        message.subscriber(), message.quote(), message.sendType());
                sendHistoryRepository.save(history);

                log.info("Email sent: {} → {}", message.sendType(), message.subscriber().getEmail());
            } catch (Exception e) {
                log.error("Failed to send email to: {}", message.subscriber().getEmail(), e);
                SendHistory history = SendHistory.failed(
                        message.subscriber(), message.quote(), message.sendType(), e.getMessage());
                sendHistoryRepository.save(history);
            }
        }
    }
}

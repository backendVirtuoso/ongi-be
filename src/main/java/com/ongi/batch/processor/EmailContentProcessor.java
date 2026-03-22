package com.ongi.batch.processor;

import com.ongi.domain.quote.entity.Quote;
import com.ongi.domain.quote.repository.QuoteRepository;
import com.ongi.domain.subscriber.entity.Subscriber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.Duration;
import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailContentProcessor implements ItemProcessor<Subscriber, EmailMessage> {

    private static final String QUOTE_CACHE_KEY = "quote:batch:%s:%s";
    private static final Duration QUOTE_TTL = Duration.ofHours(12);

    private final QuoteRepository quoteRepository;
    private final StringRedisTemplate redisTemplate;
    private final TemplateEngine templateEngine;

    private String sendType = "MORNING";
    private String templateName = "email/morning-email";

    public void configureSendType(String sendType, String templateName) {
        this.sendType = sendType;
        this.templateName = templateName;
    }

    @Override
    public EmailMessage process(@NonNull Subscriber subscriber) {
        String cacheKey = String.format(QUOTE_CACHE_KEY, LocalDate.now(), sendType);

        String cachedQuoteId = redisTemplate.opsForValue().get(cacheKey);
        Quote quote;

        if (cachedQuoteId != null) {
            quote = quoteRepository.findById(Long.parseLong(cachedQuoteId)).orElse(null);
        } else {
            quote = quoteRepository.findRandom().orElse(null);
            if (quote != null) {
                redisTemplate.opsForValue().set(cacheKey, String.valueOf(quote.getQuoteId()), QUOTE_TTL);
            }
        }

        if (quote == null) {
            log.warn("No quote found, skipping subscriber: {}", subscriber.getEmail());
            return null;
        }

        Context context = new Context();
        context.setVariable("name", subscriber.getName() != null ? subscriber.getName() : "구독자");
        context.setVariable("quoteContent", quote.getContent());
        context.setVariable("category", quote.getCategory().name());

        String html = templateEngine.process(templateName, context);
        String subject = "MORNING".equals(sendType) ? "[온기] 오늘 아침의 따뜻한 문장" : "[온기] 오늘 저녁의 따뜻한 문장";

        return new EmailMessage(subscriber, quote, sendType, subject, html);
    }
}

package com.ongi.batch.processor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ongi.batch.dto.EmailMessageDto;
import com.ongi.domain.quote.entity.Category;
import com.ongi.domain.quote.entity.Quote;
import com.ongi.domain.quote.repository.QuoteRepository;
import com.ongi.domain.subscriber.entity.Subscriber;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.infrastructure.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class EmailContentProcessor implements ItemProcessor<Subscriber, EmailMessageDto> {

    private static final String QUOTE_CACHE_KEY = "quote:batch:%s:%s";
    private static final String QUOTE_CACHE_KEY_WITH_CATEGORY = "quote:batch:%s:%s:%s";
    private static final Duration QUOTE_TTL = Duration.ofHours(12);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final QuoteRepository quoteRepository;
    private final StringRedisTemplate redisTemplate;

    @Value("#{jobParameters['sendType'] ?: 'MORNING'}")
    private String sendType;

    @Value("#{jobParameters['templateName'] ?: 'email/morning-email'}")
    private String templateName;

    @Value("${ongi.frontend.base-url}")
    private String frontendBaseUrl;

    @Override
    public EmailMessageDto process(@NonNull Subscriber subscriber) {
        Category selectedCategory = selectCategory(subscriber);
        Quote quote = resolveQuote(selectedCategory);

        if (quote == null) {
            log.warn("No quote found, skipping subscriber: {}", subscriber.getEmail());
            return null;
        }

        String subject = "MORNING".equals(sendType)
                ? "[토닥토닥] 오늘 아침의 따뜻한 문장"
                : "[토닥토닥] 오늘 저녁의 따뜻한 문장";

        return new EmailMessageDto(
                subscriber.getSubscriberId(),
                subscriber.getEmail(),
                subscriber.getName(),
                quote.getQuoteId(),
                quote.getContent(),
                quote.getCategory().name(),
                sendType,
                subject,
                templateName
        );
    }

    private Category selectCategory(Subscriber subscriber) {
        String preferredCats = subscriber.getPreferredCats();
        if (preferredCats == null || preferredCats.isBlank()) return null;
        try {
            List<String> cats = OBJECT_MAPPER.readValue(preferredCats, new TypeReference<>() {});
            if (cats.isEmpty()) return null;
            String picked = cats.get(ThreadLocalRandom.current().nextInt(cats.size()));
            return Category.valueOf(picked);
        } catch (Exception e) {
            log.warn("Failed to parse preferredCats for subscriber {}: {}", subscriber.getEmail(), preferredCats);
            return null;
        }
    }

    private Quote resolveQuote(Category category) {
        if (category != null) {
            String cacheKey = String.format(QUOTE_CACHE_KEY_WITH_CATEGORY, LocalDate.now(), sendType, category.name());
            String cachedId = redisTemplate.opsForValue().get(cacheKey);
            if (cachedId != null) {
                return quoteRepository.findById(Long.parseLong(cachedId)).orElse(null);
            }
            Quote quote = quoteRepository.findRandomByCategory(category.name()).orElse(null);
            if (quote != null) {
                redisTemplate.opsForValue().set(cacheKey, String.valueOf(quote.getQuoteId()), QUOTE_TTL);
            }
            return quote;
        }

        String cacheKey = String.format(QUOTE_CACHE_KEY, LocalDate.now(), sendType);
        String cachedId = redisTemplate.opsForValue().get(cacheKey);
        if (cachedId != null) {
            return quoteRepository.findById(Long.parseLong(cachedId)).orElse(null);
        }
        Quote quote = quoteRepository.findRandom().orElse(null);
        if (quote != null) {
            redisTemplate.opsForValue().set(cacheKey, String.valueOf(quote.getQuoteId()), QUOTE_TTL);
        }
        return quote;
    }
}

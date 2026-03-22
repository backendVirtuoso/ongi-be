package com.ongi.domain.quote.service;

import com.ongi.domain.quote.dto.QuoteResponse;
import com.ongi.domain.quote.entity.Category;
import com.ongi.domain.quote.entity.Quote;
import com.ongi.domain.quote.repository.QuoteRepository;
import com.ongi.global.exception.OngiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteService {

    private static final String TODAY_QUOTE_KEY = "quote:today:%s";
    private static final Duration TODAY_QUOTE_TTL = Duration.ofHours(12);

    private final QuoteRepository quoteRepository;
    private final StringRedisTemplate redisTemplate;

    public QuoteResponse getTodayQuote() {
        String key = String.format(TODAY_QUOTE_KEY, LocalDate.now());

        String cachedId = redisTemplate.opsForValue().get(key);
        if (cachedId != null) {
            return quoteRepository.findById(Long.parseLong(cachedId))
                    .map(QuoteResponse::from)
                    .orElseGet(this::getRandomQuote);
        }

        Quote quote = quoteRepository.findRandom()
                .orElseThrow(() -> OngiException.notFound("문장을 찾을 수 없습니다."));

        redisTemplate.opsForValue().set(key, String.valueOf(quote.getQuoteId()), TODAY_QUOTE_TTL);
        log.debug("Today's quote cached: id={}", quote.getQuoteId());

        return QuoteResponse.from(quote);
    }

    public Page<QuoteResponse> getQuotesByCategory(Category category, Pageable pageable) {
        return quoteRepository.findByCategoryAndIsActiveTrue(category, pageable)
                .map(QuoteResponse::from);
    }

    private QuoteResponse getRandomQuote() {
        return quoteRepository.findRandom()
                .map(QuoteResponse::from)
                .orElseThrow(() -> OngiException.notFound("문장을 찾을 수 없습니다."));
    }
}

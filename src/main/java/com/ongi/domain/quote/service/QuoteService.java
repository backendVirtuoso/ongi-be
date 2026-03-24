package com.ongi.domain.quote.service;

import com.ongi.domain.quote.dto.QuoteResponse;
import com.ongi.domain.quote.entity.Category;
import com.ongi.domain.quote.entity.Quote;
import com.ongi.domain.quote.repository.QuoteLikeRepository;
import com.ongi.domain.quote.repository.QuoteRepository;
import com.ongi.domain.quote.repository.QuoteSaveRepository;
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
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuoteService {

    private static final String TODAY_QUOTE_KEY = "quote:today:%s";
    private static final Duration TODAY_QUOTE_TTL = Duration.ofHours(12);

    private final QuoteRepository quoteRepository;
    private final QuoteLikeRepository quoteLikeRepository;
    private final QuoteSaveRepository quoteSaveRepository;
    private final StringRedisTemplate redisTemplate;

    public QuoteResponse getTodayQuote(Long subscriberId) {
        String key = String.format(TODAY_QUOTE_KEY, LocalDate.now());

        String cachedId = redisTemplate.opsForValue().get(key);
        Quote quote;
        if (cachedId != null) {
            quote = quoteRepository.findById(Long.parseLong(cachedId))
                    .orElseGet(() -> quoteRepository.findRandom()
                            .orElseThrow(() -> OngiException.notFound("문장을 찾을 수 없습니다.")));
        } else {
            quote = quoteRepository.findRandom()
                    .orElseThrow(() -> OngiException.notFound("문장을 찾을 수 없습니다."));
            redisTemplate.opsForValue().set(key, String.valueOf(quote.getQuoteId()), TODAY_QUOTE_TTL);
            log.debug("Today's quote cached: id={}", quote.getQuoteId());
        }

        return toResponse(quote, subscriberId);
    }

    public Page<QuoteResponse> getQuotesByCategory(Category category, Pageable pageable, Long subscriberId) {
        Page<Quote> quotes = quoteRepository.findByCategoryAndIsActiveTrue(category, pageable);

        if (subscriberId == null) {
            return quotes.map(QuoteResponse::from);
        }

        List<Long> quoteIds = quotes.map(Quote::getQuoteId).toList();
        Set<Long> likedIds = quoteLikeRepository.findLikedQuoteIds(subscriberId, quoteIds);
        Set<Long> savedIds = quoteSaveRepository.findSavedQuoteIds(subscriberId, quoteIds);

        return quotes.map(q -> QuoteResponse.from(
                q,
                likedIds.contains(q.getQuoteId()),
                savedIds.contains(q.getQuoteId())
        ));
    }

    private QuoteResponse toResponse(Quote quote, Long subscriberId) {
        if (subscriberId == null) return QuoteResponse.from(quote);

        boolean isLiked = quoteLikeRepository
                .existsByQuote_QuoteIdAndSubscriber_SubscriberId(quote.getQuoteId(), subscriberId);
        boolean isSaved = quoteSaveRepository
                .existsBySubscriber_SubscriberIdAndQuote_QuoteId(subscriberId, quote.getQuoteId());

        return QuoteResponse.from(quote, isLiked, isSaved);
    }
}

package com.ongi.domain.quote.service;

import com.ongi.domain.quote.dto.InteractionResponse;
import com.ongi.domain.quote.dto.QuoteResponse;
import com.ongi.domain.quote.entity.Quote;
import com.ongi.domain.quote.entity.QuoteLike;
import com.ongi.domain.quote.entity.QuoteSave;
import com.ongi.domain.quote.repository.QuoteLikeRepository;
import com.ongi.domain.quote.repository.QuoteRepository;
import com.ongi.domain.quote.repository.QuoteSaveRepository;
import com.ongi.domain.subscriber.entity.Subscriber;
import com.ongi.domain.subscriber.repository.SubscriberRepository;
import com.ongi.global.exception.OngiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class QuoteInteractionService {

    private final QuoteRepository quoteRepository;
    private final QuoteLikeRepository quoteLikeRepository;
    private final QuoteSaveRepository quoteSaveRepository;
    private final SubscriberRepository subscriberRepository;

    public InteractionResponse toggleLike(Long quoteId, Long subscriberId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> OngiException.notFound("문장을 찾을 수 없습니다."));
        Subscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> OngiException.notFound("구독자를 찾을 수 없습니다."));

        Optional<QuoteLike> existing = quoteLikeRepository
                .findByQuote_QuoteIdAndSubscriber_SubscriberId(quoteId, subscriberId);

        boolean isLiked;
        if (existing.isPresent()) {
            quoteLikeRepository.delete(existing.get());
            quote.decrementLikeCount();
            isLiked = false;
        } else {
            quoteLikeRepository.save(QuoteLike.create(quote, subscriber));
            quote.incrementLikeCount();
            isLiked = true;
        }

        boolean isSaved = quoteSaveRepository
                .existsBySubscriber_SubscriberIdAndQuote_QuoteId(subscriberId, quoteId);

        return new InteractionResponse(quoteId, isLiked, isSaved, quote.getLikeCount());
    }

    public InteractionResponse toggleSave(Long quoteId, Long subscriberId) {
        Quote quote = quoteRepository.findById(quoteId)
                .orElseThrow(() -> OngiException.notFound("문장을 찾을 수 없습니다."));
        Subscriber subscriber = subscriberRepository.findById(subscriberId)
                .orElseThrow(() -> OngiException.notFound("구독자를 찾을 수 없습니다."));

        Optional<QuoteSave> existing = quoteSaveRepository
                .findBySubscriber_SubscriberIdAndQuote_QuoteId(subscriberId, quoteId);

        boolean isSaved;
        if (existing.isPresent()) {
            quoteSaveRepository.delete(existing.get());
            isSaved = false;
        } else {
            quoteSaveRepository.save(QuoteSave.create(subscriber, quote));
            isSaved = true;
        }

        boolean isLiked = quoteLikeRepository
                .existsByQuote_QuoteIdAndSubscriber_SubscriberId(quoteId, subscriberId);

        return new InteractionResponse(quoteId, isLiked, isSaved, quote.getLikeCount());
    }

    @Transactional(readOnly = true)
    public List<QuoteResponse> getSavedQuotes(Long subscriberId) {
        return quoteSaveRepository
                .findBySubscriber_SubscriberIdOrderByCreatedAtDesc(subscriberId)
                .stream()
                .map(qs -> QuoteResponse.from(
                        qs.getQuote(),
                        quoteLikeRepository.existsByQuote_QuoteIdAndSubscriber_SubscriberId(
                                qs.getQuote().getQuoteId(), subscriberId),
                        true
                ))
                .toList();
    }
}

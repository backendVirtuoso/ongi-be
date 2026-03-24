package com.ongi.domain.quote.repository;

import com.ongi.domain.quote.entity.QuoteSave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface QuoteSaveRepository extends JpaRepository<QuoteSave, Long> {

    Optional<QuoteSave> findBySubscriber_SubscriberIdAndQuote_QuoteId(Long subscriberId, Long quoteId);

    boolean existsBySubscriber_SubscriberIdAndQuote_QuoteId(Long subscriberId, Long quoteId);

    List<QuoteSave> findBySubscriber_SubscriberIdOrderByCreatedAtDesc(Long subscriberId);

    @Query("SELECT qs.quote.quoteId FROM QuoteSave qs WHERE qs.subscriber.subscriberId = :subscriberId AND qs.quote.quoteId IN :quoteIds")
    Set<Long> findSavedQuoteIds(@Param("subscriberId") Long subscriberId, @Param("quoteIds") Collection<Long> quoteIds);
}

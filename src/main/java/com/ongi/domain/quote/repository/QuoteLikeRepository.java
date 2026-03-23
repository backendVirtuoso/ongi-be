package com.ongi.domain.quote.repository;

import com.ongi.domain.quote.entity.QuoteLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface QuoteLikeRepository extends JpaRepository<QuoteLike, Long> {

    Optional<QuoteLike> findByQuote_QuoteIdAndSubscriber_SubscriberId(Long quoteId, Long subscriberId);

    boolean existsByQuote_QuoteIdAndSubscriber_SubscriberId(Long quoteId, Long subscriberId);

    @Query("SELECT ql.quote.quoteId FROM QuoteLike ql WHERE ql.subscriber.subscriberId = :subscriberId AND ql.quote.quoteId IN :quoteIds")
    Set<Long> findLikedQuoteIds(@Param("subscriberId") Long subscriberId, @Param("quoteIds") Collection<Long> quoteIds);
}

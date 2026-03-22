package com.ongi.domain.quote.dto;

import com.ongi.domain.quote.entity.Category;
import com.ongi.domain.quote.entity.Quote;
import com.ongi.domain.quote.entity.SourceType;

import java.time.LocalDateTime;

public record QuoteResponse(
        Long quoteId,
        String content,
        Category category,
        SourceType sourceType,
        int likeCount,
        LocalDateTime createdAt
) {
    public static QuoteResponse from(Quote quote) {
        return new QuoteResponse(
                quote.getQuoteId(),
                quote.getContent(),
                quote.getCategory(),
                quote.getSourceType(),
                quote.getLikeCount(),
                quote.getCreatedAt()
        );
    }
}

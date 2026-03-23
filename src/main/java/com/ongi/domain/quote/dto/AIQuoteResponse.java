package com.ongi.domain.quote.dto;

import com.ongi.domain.quote.entity.Category;

public record AIQuoteResponse(
        String quote,
        Category category,
        String sourceType
) {
    public static AIQuoteResponse of(String quote, Category category) {
        return new AIQuoteResponse(quote, category, "AI");
    }
}

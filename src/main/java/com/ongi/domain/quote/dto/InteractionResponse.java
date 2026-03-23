package com.ongi.domain.quote.dto;

public record InteractionResponse(
        Long quoteId,
        boolean isLiked,
        boolean isSaved,
        int likeCount
) {}

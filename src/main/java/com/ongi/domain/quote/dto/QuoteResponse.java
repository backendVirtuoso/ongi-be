package com.ongi.domain.quote.dto;

import com.ongi.domain.quote.entity.Category;
import com.ongi.domain.quote.entity.Quote;
import com.ongi.domain.quote.entity.SourceType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "명언 응답")
public record QuoteResponse(
        @Schema(description = "명언 ID", example = "1")
        Long quoteId,
        @Schema(description = "명언 내용", example = "작은 기쁨을 발견하는 것이 행복의 시작입니다.")
        String content,
        @Schema(description = "카테고리", example = "HEALING")
        Category category,
        @Schema(description = "출처 유형 (HUMAN / AI)", example = "HUMAN")
        SourceType sourceType,
        @Schema(description = "좋아요 수", example = "42")
        int likeCount,
        @Schema(description = "내가 좋아요 눌렀는지 여부", example = "false")
        boolean isLiked,
        @Schema(description = "내가 저장했는지 여부", example = "false")
        boolean isSaved,
        @Schema(description = "생성 일시")
        LocalDateTime createdAt
) {
    public static QuoteResponse from(Quote quote) {
        return new QuoteResponse(
                quote.getQuoteId(),
                quote.getContent(),
                quote.getCategory(),
                quote.getSourceType(),
                quote.getLikeCount(),
                false,
                false,
                quote.getCreatedAt()
        );
    }

    public static QuoteResponse from(Quote quote, boolean isLiked, boolean isSaved) {
        return new QuoteResponse(
                quote.getQuoteId(),
                quote.getContent(),
                quote.getCategory(),
                quote.getSourceType(),
                quote.getLikeCount(),
                isLiked,
                isSaved,
                quote.getCreatedAt()
        );
    }
}

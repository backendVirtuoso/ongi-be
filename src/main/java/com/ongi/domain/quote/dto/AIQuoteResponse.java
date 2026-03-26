package com.ongi.domain.quote.dto;

import com.ongi.domain.quote.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "AI 명언 생성 응답")
public record AIQuoteResponse(
        @Schema(description = "AI가 생성한 명언", example = "두려움은 성장의 문 앞에 서 있는 문지기일 뿐입니다.")
        String quote,
        @Schema(description = "카테고리", example = "COURAGE")
        Category category,
        @Schema(description = "출처 유형", example = "AI")
        String sourceType
) {
    public static AIQuoteResponse of(String quote, Category category) {
        return new AIQuoteResponse(quote, category, "AI");
    }
}

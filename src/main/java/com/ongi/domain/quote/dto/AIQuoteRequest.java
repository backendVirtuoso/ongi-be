package com.ongi.domain.quote.dto;

import com.ongi.domain.quote.entity.Category;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "AI 명언 생성 요청")
public record AIQuoteRequest(
        @Schema(description = "현재 상황 또는 감정 설명 (최대 300자)", example = "오늘 발표가 너무 긴장되고 자신이 없어요.")
        @NotBlank @Size(max = 300) String situation,
        @Schema(description = "원하는 명언 카테고리", example = "COURAGE")
        @NotNull Category category
) {}

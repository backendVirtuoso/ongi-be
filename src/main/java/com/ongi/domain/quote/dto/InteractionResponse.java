package com.ongi.domain.quote.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "좋아요/저장 토글 응답")
public record InteractionResponse(
        @Schema(description = "명언 ID", example = "1")
        Long quoteId,
        @Schema(description = "좋아요 여부", example = "true")
        boolean isLiked,
        @Schema(description = "저장 여부", example = "false")
        boolean isSaved,
        @Schema(description = "현재 좋아요 수", example = "43")
        int likeCount
) {}

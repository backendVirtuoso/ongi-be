package com.ongi.domain.subscriber.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "수신 설정 변경 요청")
public record PreferenceUpdateRequest(
        @Schema(description = "선호 카테고리 목록", example = "[\"HEALING\", \"MOTIVATION\"]")
        @NotNull List<String> preferredCategories
) {}

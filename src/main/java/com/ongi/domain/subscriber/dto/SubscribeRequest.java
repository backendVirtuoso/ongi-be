package com.ongi.domain.subscriber.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

@Schema(description = "구독 신청 요청")
public record SubscribeRequest(
        @Schema(description = "구독자 이메일", example = "user@example.com")
        @NotBlank @Email String email,
        @Schema(description = "구독자 이름", example = "홍길동")
        String name,
        @Schema(description = "선호 카테고리 목록", example = "[\"HEALING\", \"MOTIVATION\"]")
        List<String> preferredCategories
) {}

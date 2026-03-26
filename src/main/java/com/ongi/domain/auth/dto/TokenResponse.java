package com.ongi.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "JWT 토큰 발급 응답")
public record TokenResponse(
        @Schema(description = "JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,
        @Schema(description = "구독자 ID", example = "1")
        Long subscriberId,
        @Schema(description = "구독자 이메일", example = "user@example.com")
        String email
) {}

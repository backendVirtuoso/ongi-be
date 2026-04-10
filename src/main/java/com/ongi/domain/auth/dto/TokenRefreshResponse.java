package com.ongi.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "액세스 토큰 갱신 응답")
public record TokenRefreshResponse(
        @Schema(description = "새로 발급된 JWT 액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken
) {}

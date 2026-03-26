package com.ongi.domain.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "매직 링크 발송 요청")
public record MagicLinkRequest(
        @Schema(description = "로그인할 이메일 주소", example = "user@example.com")
        @NotBlank @Email String email
) {}

package com.ongi.domain.auth.dto;

public record TokenResponse(
        String accessToken,
        Long subscriberId,
        String email
) {}

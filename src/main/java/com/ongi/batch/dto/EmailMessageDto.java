package com.ongi.batch.dto;

public record EmailMessageDto(
        Long subscriberId,
        String email,
        String name,
        Long quoteId,
        String quoteContent,
        String category,
        String sendType,
        String subject,
        String templateName
) {}

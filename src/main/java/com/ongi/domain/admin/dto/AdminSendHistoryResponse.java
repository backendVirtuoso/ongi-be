package com.ongi.domain.admin.dto;

import com.ongi.domain.subscriber.entity.SendHistory;

import java.time.LocalDateTime;

public record AdminSendHistoryResponse(
        Long historyId,
        String email,
        String quoteContent,
        String sendType,
        String sendStatus,
        LocalDateTime sentAt,
        String errorMessage
) {
    public static AdminSendHistoryResponse from(SendHistory h) {
        return new AdminSendHistoryResponse(
                h.getHistoryId(),
                h.getSubscriber().getEmail(),
                h.getQuote().getContent(),
                h.getSendType(),
                h.getSendStatus(),
                h.getSentAt(),
                h.getErrorMessage()
        );
    }
}

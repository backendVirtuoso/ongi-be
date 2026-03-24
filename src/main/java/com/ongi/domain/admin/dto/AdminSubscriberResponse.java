package com.ongi.domain.admin.dto;

import com.ongi.domain.subscriber.entity.Subscriber;

import java.time.LocalDateTime;

public record AdminSubscriberResponse(
        Long subscriberId,
        String email,
        String name,
        String status,
        LocalDateTime subscribedAt,
        LocalDateTime verifiedAt
) {
    public static AdminSubscriberResponse from(Subscriber s) {
        return new AdminSubscriberResponse(
                s.getSubscriberId(),
                s.getEmail(),
                s.getName(),
                s.getStatus().name(),
                s.getSubscribedAt(),
                s.getVerifiedAt()
        );
    }
}

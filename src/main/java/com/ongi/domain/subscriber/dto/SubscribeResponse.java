package com.ongi.domain.subscriber.dto;

import com.ongi.domain.subscriber.entity.Subscriber;
import com.ongi.domain.subscriber.entity.SubscriberStatus;

public record SubscribeResponse(
        Long subscriberId,
        String email,
        SubscriberStatus status,
        String message
) {
    public static SubscribeResponse from(Subscriber subscriber, String message) {
        return new SubscribeResponse(
                subscriber.getSubscriberId(),
                subscriber.getEmail(),
                subscriber.getStatus(),
                message
        );
    }
}

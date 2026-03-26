package com.ongi.domain.subscriber.dto;

import com.ongi.domain.subscriber.entity.Subscriber;
import com.ongi.domain.subscriber.entity.SubscriberStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "구독 응답")
public record SubscribeResponse(
        @Schema(description = "구독자 ID", example = "1")
        Long subscriberId,
        @Schema(description = "구독자 이메일", example = "user@example.com")
        String email,
        @Schema(description = "구독 상태 (PENDING / ACTIVE / UNSUBSCRIBED)", example = "PENDING")
        SubscriberStatus status,
        @Schema(description = "안내 메시지", example = "인증 메일을 발송했습니다.")
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

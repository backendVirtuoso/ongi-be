package com.ongi.domain.subscriber.dto;

import com.ongi.domain.subscriber.entity.Subscriber;
import com.ongi.domain.subscriber.entity.SubscriberStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
@Schema(description = "내 정보 응답")
public record SubscriberMeResponse(
        @Schema(description = "구독자 ID", example = "1")
        Long subscriberId,
        @Schema(description = "이메일", example = "user@example.com")
        String email,
        @Schema(description = "이름", example = "홍길동")
        String name,
        @Schema(description = "구독 상태", example = "ACTIVE")
        SubscriberStatus status,
        @Schema(description = "선호 카테고리 목록", example = "[\"HEALING\", \"MOTIVATION\"]")
        List<String> preferredCategories,
        @Schema(description = "관리자 여부", example = "false")
        boolean isAdmin
) {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static SubscriberMeResponse from(Subscriber subscriber) {
        List<String> categories = parseCategories(subscriber.getPreferredCats());
        return new SubscriberMeResponse(
                subscriber.getSubscriberId(),
                subscriber.getEmail(),
                subscriber.getName(),
                subscriber.getStatus(),
                categories,
                subscriber.isAdmin()
        );
    }

    private static List<String> parseCategories(String json) {
        if (json == null || json.isBlank()) return List.of();
        try {
            return OBJECT_MAPPER.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            log.warn("Failed to parse preferred categories JSON: {}", json);
            return List.of();
        }
    }
}

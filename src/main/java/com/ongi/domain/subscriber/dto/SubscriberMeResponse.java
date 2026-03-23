package com.ongi.domain.subscriber.dto;

import com.ongi.domain.subscriber.entity.Subscriber;
import com.ongi.domain.subscriber.entity.SubscriberStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public record SubscriberMeResponse(
        Long subscriberId,
        String email,
        String name,
        SubscriberStatus status,
        List<String> preferredCategories
) {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static SubscriberMeResponse from(Subscriber subscriber) {
        List<String> categories = parseCategories(subscriber.getPreferredCats());
        return new SubscriberMeResponse(
                subscriber.getSubscriberId(),
                subscriber.getEmail(),
                subscriber.getName(),
                subscriber.getStatus(),
                categories
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

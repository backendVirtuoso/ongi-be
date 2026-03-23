package com.ongi.infra.claude;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ClaudeClient {

    private final RestClient restClient;
    private final String model;

    public ClaudeClient(
            @Value("${ongi.claude.api-key}") String apiKey,
            @Value("${ongi.claude.base-url}") String baseUrl,
            @Value("${ongi.claude.model}") String model
    ) {
        this.model = model;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("x-api-key", apiKey)
                .defaultHeader("anthropic-version", "2023-06-01")
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String generateQuote(String prompt) {
        Map<String, Object> body = Map.of(
                "model", model,
                "max_tokens", 256,
                "messages", List.of(Map.of("role", "user", "content", prompt))
        );

        ClaudeApiResponse response = restClient.post()
                .uri("/v1/messages")
                .body(body)
                .retrieve()
                .body(ClaudeApiResponse.class);

        if (response == null || response.content() == null || response.content().isEmpty()) {
            throw new RuntimeException("Claude API 응답이 비어있습니다.");
        }

        return response.content().get(0).text().strip();
    }
}

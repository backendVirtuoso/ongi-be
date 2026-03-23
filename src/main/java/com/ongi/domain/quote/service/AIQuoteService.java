package com.ongi.domain.quote.service;

import com.ongi.domain.quote.dto.AIQuoteRequest;
import com.ongi.domain.quote.dto.AIQuoteResponse;
import com.ongi.domain.quote.entity.Category;
import com.ongi.global.exception.OngiException;
import com.ongi.infra.claude.ClaudeClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIQuoteService {

    private static final Map<Category, String> CATEGORY_KO = Map.of(
            Category.COMFORT, "위로",
            Category.CHEER, "응원",
            Category.ENCOURAGE, "격려",
            Category.SUPPORT, "지지",
            Category.CELEBRATE, "축하",
            Category.LOVE, "사랑"
    );

    private final ClaudeClient claudeClient;

    public AIQuoteResponse generate(AIQuoteRequest request) {
        String prompt = buildPrompt(request.situation(), request.category());
        try {
            String quote = claudeClient.generateQuote(prompt);
            log.info("AI quote generated for category={}", request.category());
            return AIQuoteResponse.of(quote, request.category());
        } catch (Exception e) {
            log.error("Failed to generate AI quote", e);
            throw OngiException.badRequest("AI 문장 생성에 실패했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    private String buildPrompt(String situation, Category category) {
        String categoryKo = CATEGORY_KO.getOrDefault(category, "위로");
        return String.format(
                "당신은 따뜻하고 진심 어린 문장을 쓰는 전문가입니다. " +
                "다음 상황에 처한 사람에게 어울리는 '%s' 문장을 한 문장으로 작성해주세요. " +
                "문장만 출력하고 다른 설명은 하지 마세요.\n\n상황: %s",
                categoryKo, situation
        );
    }
}

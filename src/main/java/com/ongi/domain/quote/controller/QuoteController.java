package com.ongi.domain.quote.controller;

import com.ongi.domain.quote.dto.InteractionResponse;
import com.ongi.domain.quote.dto.QuoteResponse;
import com.ongi.domain.quote.entity.Category;
import com.ongi.domain.quote.service.AIQuoteService;
import com.ongi.domain.quote.service.QuoteInteractionService;
import com.ongi.domain.quote.service.QuoteService;
import com.ongi.domain.quote.dto.AIQuoteRequest;
import com.ongi.domain.quote.dto.AIQuoteResponse;
import com.ongi.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Quote", description = "명언 조회 및 상호작용")
@RestController
@RequestMapping("/api/v1/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;
    private final QuoteInteractionService quoteInteractionService;
    private final AIQuoteService aiQuoteService;

    @Operation(summary = "오늘의 명언 조회", description = "오늘의 명언을 반환합니다. 로그인 시 좋아요/저장 여부도 포함됩니다.")
    @GetMapping("/today")
    public ResponseEntity<ApiResponse<QuoteResponse>> getTodayQuote(Authentication authentication) {
        Long subscriberId = subscriberId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(quoteService.getTodayQuote(subscriberId)));
    }

    @Operation(summary = "카테고리별 명언 목록 조회", description = "카테고리와 페이지 정보를 기반으로 명언 목록을 반환합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<QuoteResponse>>> getQuotesByCategory(
            @RequestParam Category category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Authentication authentication
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(
                quoteService.getQuotesByCategory(category, pageable, subscriberId(authentication))));
    }

    private Long subscriberId(Authentication authentication) {
        return authentication != null ? (Long) authentication.getPrincipal() : null;
    }

    @Operation(summary = "명언 좋아요 토글", description = "명언에 좋아요를 추가하거나 취소합니다. (로그인 필요)")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<InteractionResponse>> toggleLike(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long subscriberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(quoteInteractionService.toggleLike(id, subscriberId)));
    }

    @Operation(summary = "명언 저장 토글", description = "명언을 저장하거나 저장 취소합니다. (로그인 필요)")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/{id}/save")
    public ResponseEntity<ApiResponse<InteractionResponse>> toggleSave(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long subscriberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(quoteInteractionService.toggleSave(id, subscriberId)));
    }

    @Operation(summary = "저장된 명언 목록 조회", description = "내가 저장한 명언 목록을 반환합니다. (로그인 필요)")
    @SecurityRequirement(name = "BearerAuth")
    @GetMapping("/saved")
    public ResponseEntity<ApiResponse<List<QuoteResponse>>> getSavedQuotes(Authentication authentication) {
        Long subscriberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(quoteInteractionService.getSavedQuotes(subscriberId)));
    }

    @Operation(summary = "AI 명언 생성", description = "Claude AI를 통해 맞춤 명언을 생성합니다. (로그인 필요)")
    @SecurityRequirement(name = "BearerAuth")
    @PostMapping("/ai-generate")
    public ResponseEntity<ApiResponse<AIQuoteResponse>> generateAIQuote(
            @Valid @RequestBody AIQuoteRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(aiQuoteService.generate(request)));
    }
}

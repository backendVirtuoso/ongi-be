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
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;
    private final QuoteInteractionService quoteInteractionService;
    private final AIQuoteService aiQuoteService;

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<QuoteResponse>> getTodayQuote(Authentication authentication) {
        Long subscriberId = subscriberId(authentication);
        return ResponseEntity.ok(ApiResponse.ok(quoteService.getTodayQuote(subscriberId)));
    }

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

    @PostMapping("/{id}/like")
    public ResponseEntity<ApiResponse<InteractionResponse>> toggleLike(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long subscriberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(quoteInteractionService.toggleLike(id, subscriberId)));
    }

    @PostMapping("/{id}/save")
    public ResponseEntity<ApiResponse<InteractionResponse>> toggleSave(
            @PathVariable Long id,
            Authentication authentication
    ) {
        Long subscriberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(quoteInteractionService.toggleSave(id, subscriberId)));
    }

    @GetMapping("/saved")
    public ResponseEntity<ApiResponse<List<QuoteResponse>>> getSavedQuotes(Authentication authentication) {
        Long subscriberId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ApiResponse.ok(quoteInteractionService.getSavedQuotes(subscriberId)));
    }

    @PostMapping("/ai-generate")
    public ResponseEntity<ApiResponse<AIQuoteResponse>> generateAIQuote(
            @Valid @RequestBody AIQuoteRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(aiQuoteService.generate(request)));
    }
}

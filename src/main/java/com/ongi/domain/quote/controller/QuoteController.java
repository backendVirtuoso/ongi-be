package com.ongi.domain.quote.controller;

import com.ongi.domain.quote.dto.QuoteResponse;
import com.ongi.domain.quote.entity.Category;
import com.ongi.domain.quote.service.QuoteService;
import com.ongi.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/quotes")
@RequiredArgsConstructor
public class QuoteController {

    private final QuoteService quoteService;

    @GetMapping("/today")
    public ResponseEntity<ApiResponse<QuoteResponse>> getTodayQuote() {
        return ResponseEntity.ok(ApiResponse.ok(quoteService.getTodayQuote()));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<QuoteResponse>>> getQuotesByCategory(
            @RequestParam Category category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.ok(quoteService.getQuotesByCategory(category, pageable)));
    }
}

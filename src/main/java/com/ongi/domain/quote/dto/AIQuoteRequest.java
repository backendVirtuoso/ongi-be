package com.ongi.domain.quote.dto;

import com.ongi.domain.quote.entity.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AIQuoteRequest(
        @NotBlank @Size(max = 300) String situation,
        @NotNull Category category
) {}

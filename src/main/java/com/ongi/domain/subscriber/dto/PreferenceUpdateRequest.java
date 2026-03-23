package com.ongi.domain.subscriber.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record PreferenceUpdateRequest(
        @NotNull List<String> preferredCategories
) {}

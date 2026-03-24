package com.ongi.domain.subscriber.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record SubscribeRequest(
        @NotBlank @Email String email,
        String name,
        List<String> preferredCategories
) {}

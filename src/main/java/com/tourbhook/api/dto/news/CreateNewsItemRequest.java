package com.tourbhook.api.dto.news;

import com.tourbhook.api.entity.NewsType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record CreateNewsItemRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 300, message = "Title must be at most 300 characters")
        String title,

        @Size(max = 2000, message = "Summary must be at most 2000 characters")
        String summary,

        String sourceUrl,

        @NotBlank(message = "Region is required")
        String region,

        @NotBlank(message = "Category is required")
        String category,

        @NotNull(message = "Type is required (ADVISORY or GENERAL)")
        NewsType type,

        Instant publishedAt
) {
}
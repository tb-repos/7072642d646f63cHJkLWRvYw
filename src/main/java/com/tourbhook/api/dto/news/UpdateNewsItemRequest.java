package com.tourbhook.api.dto.news;

import com.tourbhook.api.entity.NewsType;
import jakarta.validation.constraints.Size;

public record UpdateNewsItemRequest(
        @Size(max = 300, message = "Title must be at most 300 characters")
        String title,

        @Size(max = 2000, message = "Summary must be at most 2000 characters")
        String summary,

        String sourceUrl,
        String region,
        String category,
        NewsType type
) {
}
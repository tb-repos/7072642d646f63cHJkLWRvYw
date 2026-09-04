package com.tourbhook.api.dto.news;

import com.tourbhook.api.entity.NewsType;

import java.time.Instant;

public record NewsItemResponse(
        String id,
        String title,
        String summary,
        String sourceUrl,
        String region,
        String category,
        NewsType type,
        Instant publishedAt
) {
}
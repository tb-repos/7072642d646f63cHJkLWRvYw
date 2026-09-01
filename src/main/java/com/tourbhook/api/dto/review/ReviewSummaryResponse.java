package com.tourbhook.api.dto.review;

import java.time.Instant;

public record ReviewSummaryResponse(
        String placeId,
        String summary,
        int reviewsCount,
        Instant generatedAt
) {
}
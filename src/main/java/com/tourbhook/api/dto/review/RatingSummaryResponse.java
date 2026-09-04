package com.tourbhook.api.dto.review;

import java.math.BigDecimal;

public record RatingSummaryResponse(
        String placeId,
        BigDecimal averageRating,
        long reviewsCount
) {
}
package com.shrinetours.api.dto.place;

import java.math.BigDecimal;

public record PlaceResponse(
        String id,
        String name,
        String category,
        String imageUrl,
        String typicalDuration,
        Double latitude,
        Double longitude,
        BigDecimal rating,
        Integer reviewsCount,
        boolean verified
) {
}
package com.shrinetours.api.dto.trip;

import java.math.BigDecimal;

public record TripStartingPointDto(
        String id,
        String name,
        String category,
        String imageUrl,
        String typicalDuration,
        Double latitude,
        Double longitude,
        BigDecimal rating,
        Integer reviewsCount,
        Boolean verified
) {
}
package com.shrinetours.api.dto.itinerary;

import java.math.BigDecimal;

public record ActivityResponse(
        String id,
        String activityTime,
        String title,
        String duration,
        BigDecimal cost,
        String icon,
        String placeId
) {
}
package com.shrinetours.api.dto.trip;

import java.time.LocalDate;

public record TripSummaryResponse(
        String id,
        String city,
        String imageUrl,
        LocalDate startDate,
        LocalDate endDate,
        Integer placesCount,
        Integer days,
        Integer adults,
        Integer kids,
        String tripstyle,
        String purposeOfTravel
) {
}
package com.shrinetours.api.dto.itinerary;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateDayRequest(
        @NotNull(message = "Day number is required")
        Integer day_number,
        List<ActivityResponse> activities
) {
}

package com.tourbhook.api.dto.itinerary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GenerateItineraryRequest(
        @NotBlank(message = "Trip id is required")
        String trip_id,
        @NotBlank(message = "City is required")
        String city,
        @NotNull(message = "Days is required")
        Integer days
) {
}

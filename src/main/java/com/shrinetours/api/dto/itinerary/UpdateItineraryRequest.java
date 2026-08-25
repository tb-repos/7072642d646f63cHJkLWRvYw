package com.shrinetours.api.dto.itinerary;

import jakarta.validation.constraints.NotBlank;

public record UpdateItineraryRequest(
        @NotBlank(message = "itineraryId is required")
        String itineraryId,

        @NotBlank(message = "tripId is required")
        String tripId,

        @NotBlank(message = "city is required")
        String city,

        Integer days
) {
}
package com.shrinetours.api.dto.place;

import jakarta.validation.constraints.NotBlank;

public record AddPlaceToTripRequest(
        @NotBlank(message = "Trip id is required")
        String trip_id,

        @NotBlank(message = "Place id is required")
        String place_id
) {
}
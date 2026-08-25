package com.shrinetours.api.dto.traveller;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateTravellerRequest(
        @NotBlank(message = "Trip id is required")
        String trip_id,
        @NotNull(message = "Adults is required")
        @Min(value = 1, message = "At least one adult is required")
        Integer adults,
        @NotNull(message = "Kids is required")
        @Min(value = 0, message = "Kids cannot be negative")
        Integer kids
) {
}

package com.shrinetours.api.dto.itinerary;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AddActivityRequest(
        @NotNull(message = "Day number is required")
        Integer day_number,

        @NotBlank(message = "Activity time is required")
        String activity_time,

        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Duration is required")
        String duration,

        @NotNull(message = "Cost is required")
        BigDecimal cost,

        @NotBlank(message = "Icon is required")
        String icon,

        String placeId
) {
}
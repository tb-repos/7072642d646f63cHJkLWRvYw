package com.shrinetours.api.dto.trip;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record CreateTripRequest(
        @NotBlank String city,

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate start_date,

        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate end_date,

        @NotNull @Min(1)
        Integer adults,

        @NotNull @Min(0)
        Integer kids,
        Integer trip_style,
        String purpose_of_travel,
        TripStartingPointDto starting_point
) {
}
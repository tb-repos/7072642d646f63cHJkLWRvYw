package com.shrinetours.api.dto.trip;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public record UpdateTripRequest(
        String city,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate start_date,
        @JsonFormat(pattern = "yyyy-MM-dd")
        LocalDate end_date,
        Integer adults,
        Integer kids,
        String trip_style,
        String purpose_of_travel,
        String image_url,
        List<String> place_ids,
        TripStartingPointDto starting_point
) {
}
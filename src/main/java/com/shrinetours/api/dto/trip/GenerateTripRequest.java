package com.shrinetours.api.dto.trip;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.shrinetours.api.dto.place.PlaceResponse;
import com.shrinetours.api.entity.Place;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

public record GenerateTripRequest(
        @NotBlank String city,
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate start_date,
        @NotNull @JsonFormat(pattern = "yyyy-MM-dd") LocalDate end_date,
        @NotNull Integer adults,
        @NotNull Integer kids,
        List<String> selected_places,
        String trip_style,
        String purpose_of_travel,
        TripStartingPointDto starting_point
        
        
         
) {

	}
package com.shrinetours.api.dto.trip;

import java.time.LocalDate;
import java.util.List;

import com.shrinetours.api.dto.place.PlaceResponse;

public record GeneratedTripResponse(
        String tripId,
        String itineraryId,
        String city,
        LocalDate startDate,
        LocalDate endDate,
        Integer days,
        String tripStyle,
        String purposeOfTravel,
        TripStartingPointDto starting_point,
        List<String> selected_places

) {
}
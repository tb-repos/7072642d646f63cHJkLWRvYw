package com.shrinetours.api.dto.trip;

import com.shrinetours.api.dto.place.PlaceResponse;

import java.time.LocalDate;
import java.util.List;

public record TripDetailResponse(
        String id,
        String city,
        String imageUrl,
        LocalDate startDate,
        LocalDate endDate,
        Integer placesCount,
        Integer days,
        Integer adults,
        Integer kids,
        String tripStyle,
        String purposeOfTravel,
        String itineraryId,
        List<PlaceResponse> places,
        TripStartingPointDto starting_point
) {
}
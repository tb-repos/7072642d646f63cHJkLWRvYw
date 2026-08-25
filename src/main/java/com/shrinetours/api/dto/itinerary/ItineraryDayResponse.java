package com.shrinetours.api.dto.itinerary;

import com.shrinetours.api.dto.place.PlaceResponse;

import java.util.List;

public record ItineraryDayResponse(
        Integer dayNumber,
        List<ActivityResponse> activities,
        List<PlaceResponse> places
) {
}
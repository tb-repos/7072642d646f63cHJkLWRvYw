package com.tourbhook.api.dto.itinerary;

import com.tourbhook.api.dto.place.PlaceResponse;

import java.util.List;

public record ItineraryDayResponse(
        Integer dayNumber,
        List<ActivityResponse> activities,
        List<PlaceResponse> places
) {
}
package com.shrinetours.api.dto.itinerary;

import java.util.List;

public record ItineraryResponse(
        String id,
        String tripId,
        String city,
        String title,
        
        List<ItineraryDayResponse> days
) {
}

package com.shrinetours.api.dto.traveller;

public record TravellerResponse(
        String tripId,
        Integer adults,
        Integer kids
) {
}

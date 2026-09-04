package com.tourbhook.api.dto.traveller;

public record TravellerResponse(
        String tripId,
        Integer adults,
        Integer kids
) {
}

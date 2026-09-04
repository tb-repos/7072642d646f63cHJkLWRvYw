package com.tourbhook.api.dto.routing;

public record RouteLegResponse(
        String fromPlaceId,
        String fromName,
        String toPlaceId,
        String toName,
        long distanceMeters,
        long durationSeconds
) {
}
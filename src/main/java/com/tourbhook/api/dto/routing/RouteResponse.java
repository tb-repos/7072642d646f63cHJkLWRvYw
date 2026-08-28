package com.tourbhook.api.dto.routing;

import java.util.List;

public record RouteResponse(
        String travelMode,
        List<String> placeIdsInOrder,
        long totalDistanceMeters,
        long totalDurationSeconds,
        String overviewPolyline,
        List<RouteLegResponse> legs
) {
}
package com.tourbhook.api.dto.routing;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record ComputeRouteRequest(
        @NotEmpty(message = "At least two place IDs are required to compute a route")
        @Size(min = 2, message = "At least two place IDs are required to compute a route")
        List<String> placeIds,

        String travelMode
) {
}
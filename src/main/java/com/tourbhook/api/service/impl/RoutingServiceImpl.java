package com.tourbhook.api.service.impl;

import com.tourbhook.api.dto.routing.ComputeRouteRequest;
import com.tourbhook.api.dto.routing.RouteLegResponse;
import com.tourbhook.api.dto.routing.RouteResponse;
import com.tourbhook.api.entity.Place;
import com.tourbhook.api.repository.PlaceRepository;
import com.tourbhook.api.repository.exception.BadRequestException;
import com.tourbhook.api.repository.exception.ExternalServiceException;
import com.tourbhook.api.repository.exception.ResourceNotFoundException;
import com.tourbhook.api.service.RoutingService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RoutingServiceImpl implements RoutingService {

    private static final Set<String> ALLOWED_TRAVEL_MODES = Set.of("driving", "walking", "bicycling", "transit");
    private static final String DEFAULT_TRAVEL_MODE = "driving";

    private final PlaceRepository placeRepository;
    private final GoogleMapsClient googleMapsClient;

    public RoutingServiceImpl(PlaceRepository placeRepository, GoogleMapsClient googleMapsClient) {
        this.placeRepository = placeRepository;
        this.googleMapsClient = googleMapsClient;
    }

    @Override
    public RouteResponse computeRoute(ComputeRouteRequest request) {
        if (googleMapsClient.getApiKey() == null || googleMapsClient.getApiKey().isBlank()) {
            throw new ExternalServiceException(
                    "Google Maps API key is not configured (GOOGLE_MAPS_API_KEY) — routing is unavailable");
        }

        String travelMode = normalizeTravelMode(request.travelMode());
        List<Place> places = resolvePlacesInRequestedOrder(request.placeIds());

        for (Place place : places) {
            if (place.getLatitude() == null || place.getLongitude() == null) {
                throw new BadRequestException(
                        "Destination '" + place.getName() + "' has no coordinates and can't be routed");
            }
        }

        String origin = toLatLng(places.get(0));
        String destination = toLatLng(places.get(places.size() - 1));
        String waypoints = places.subList(1, places.size() - 1).stream()
                .map(this::toLatLng)
                .collect(Collectors.joining("|"));

        Map<String, Object> directions = googleMapsClient.getDirections(origin, destination, waypoints, travelMode);
        return buildRouteResponse(directions, places, travelMode);
    }

    private List<Place> resolvePlacesInRequestedOrder(List<String> placeIds) {
        Map<String, Place> byId = placeRepository.findAllById(placeIds).stream()
                .collect(Collectors.toMap(Place::getId, place -> place));

        List<Place> ordered = new ArrayList<>(placeIds.size());
        for (String placeId : placeIds) {
            Place place = byId.get(placeId);
            if (place == null) {
                throw new ResourceNotFoundException("Destination not found: " + placeId);
            }
            ordered.add(place);
        }
        return ordered;
    }

    private String normalizeTravelMode(String requested) {
        if (requested == null || requested.isBlank()) {
            return DEFAULT_TRAVEL_MODE;
        }
        String normalized = requested.toLowerCase(Locale.ROOT).trim();
        if (!ALLOWED_TRAVEL_MODES.contains(normalized)) {
            throw new BadRequestException("travelMode must be one of " + ALLOWED_TRAVEL_MODES);
        }
        return normalized;
    }

    private String toLatLng(Place place) {
        return place.getLatitude() + "," + place.getLongitude();
    }

    @SuppressWarnings("unchecked")
    private RouteResponse buildRouteResponse(Map<String, Object> directions, List<Place> places, String travelMode) {
        String status = String.valueOf(directions.get("status"));
        if (!"OK".equals(status)) {
            String errorMessage = String.valueOf(directions.getOrDefault("error_message", "no further detail"));
            throw new ExternalServiceException("Google Directions API returned " + status + ": " + errorMessage);
        }

        List<Object> routes = (List<Object>) directions.get("routes");
        if (routes == null || routes.isEmpty()) {
            throw new ExternalServiceException("Google Directions API returned no routes");
        }

        Map<String, Object> route = (Map<String, Object>) routes.get(0);
        List<Object> apiLegs = (List<Object>) route.get("legs");
        if (apiLegs == null || apiLegs.size() != places.size() - 1) {
            throw new ExternalServiceException("Google Directions API returned an unexpected number of legs");
        }

        List<RouteLegResponse> legs = new ArrayList<>(apiLegs.size());
        long totalDistanceMeters = 0;
        long totalDurationSeconds = 0;

        for (int i = 0; i < apiLegs.size(); i++) {
            Map<String, Object> apiLeg = (Map<String, Object>) apiLegs.get(i);
            long distanceMeters = extractMetricValue(apiLeg, "distance");
            long durationSeconds = extractMetricValue(apiLeg, "duration");

            Place from = places.get(i);
            Place to = places.get(i + 1);

            legs.add(new RouteLegResponse(from.getId(), from.getName(), to.getId(), to.getName(),
                    distanceMeters, durationSeconds));

            totalDistanceMeters += distanceMeters;
            totalDurationSeconds += durationSeconds;
        }

        String overviewPolyline = extractOverviewPolyline(route);

        return new RouteResponse(
                travelMode,
                places.stream().map(Place::getId).toList(),
                totalDistanceMeters,
                totalDurationSeconds,
                overviewPolyline,
                legs
        );
    }

    @SuppressWarnings("unchecked")
    private long extractMetricValue(Map<String, Object> leg, String key) {
        Object metric = leg.get(key);
        if (!(metric instanceof Map<?, ?> metricMap)) {
            return 0L;
        }
        Object value = metricMap.get("value");
        return value instanceof Number number ? number.longValue() : 0L;
    }

    @SuppressWarnings("unchecked")
    private String extractOverviewPolyline(Map<String, Object> route) {
        Object polylineObj = route.get("overview_polyline");
        if (!(polylineObj instanceof Map<?, ?> polylineMap)) {
            return null;
        }
        Object points = polylineMap.get("points");
        return points != null ? points.toString() : null;
    }
}